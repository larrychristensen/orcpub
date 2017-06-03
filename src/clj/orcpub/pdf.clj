(ns orcpub.pdf
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.dnd.e5.display :as dis5e]
            [clojure.java.io :as io])
  (:import (org.apache.pdfbox.pdmodel.interactive.form PDCheckBox PDComboBox PDListBox PDRadioButton PDTextField)
           (org.apache.pdfbox.pdmodel PDDocument PDPageContentStream)
           (org.apache.pdfbox.pdmodel.graphics.image PDImageXObject)
           (java.io ByteArrayOutputStream ByteArrayInputStream)
           (org.apache.pdfbox.pdmodel.graphics.image JPEGFactory LosslessFactory)
           (org.apache.pdfbox.pdmodel.font PDType1Font)
           (org.eclipse.jetty.server.handler.gzip GzipHandler)
           (javax.imageio ImageIO)
           (java.net URL)))

(defn write-fields! [doc fields flatten font-sizes]
  (let [catalog (.getDocumentCatalog doc)
        form (.getAcroForm catalog)]
    (.setNeedAppearances form true)
    (doseq [[k v] fields]
      (try
        (let [field (.getField form (name k))]
          (when field
            (if (and (font-sizes k) flatten)
              (.setDefaultAppearance field (str "/Helv " (font-sizes k) " Tf 0 0 0 rg")))
            (.setValue
             field
             (cond 
               (instance? PDCheckBox field) (if v "Yes" "Off")
               (instance? PDTextField field) (str v)
               :else nil))))
        (catch Exception e (prn "failed writing field: " k v (clojure.stacktrace/print-stack-trace e)))))
    (when flatten
      (.setNeedAppearances form false)
      (.flatten form))))

(defn content-stream [doc page]
  (PDPageContentStream. doc page true false true))

(defn in-to-sz [inches]
  (float (* 72 inches)))

(defn in-to-coord-x [inches]
  (in-to-sz inches))

(defn in-to-coord-y [inches]
  (in-to-sz (- 11 inches)))

(defn scale [[r-h r-w] [i-h i-w]]
  (let [height-to-width (/ i-h i-w)
        rect-height-to-width (/ r-h r-w)
        height-ratio (/ r-h i-h)]
    (if (> height-to-width rect-height-to-width)
      [r-h (* r-h (/ i-w i-h))]
      [(* r-w (/ i-h i-w)) r-w])))

(defn draw-imagex [c-stream img x y width height]
  (let [[scaled-height scaled-width] (scale [height width] [(.getHeight img) (.getWidth img)])]
    (.drawImage
     c-stream
     img
     (in-to-coord-x (+ x (if (< scaled-width width)
                           (/ (- width scaled-width) 2)
                           0)))
     (in-to-coord-y (+ height y (if (< scaled-height height)
                                  (/ (- scaled-height height) 2)
                                  0)))
     (in-to-sz scaled-width)
     (in-to-sz scaled-height))))

(defn draw-non-jpg [doc page url x y width height]
  (with-open [c-stream (content-stream doc page)]
    (let [img (LosslessFactory/createFromImage doc (ImageIO/read (URL. url)))]
      (draw-imagex c-stream img x y width height))))

(defn draw-jpg [doc page url x y width height]
  (with-open [c-stream (content-stream doc page)
              image-stream (.openStream (URL. url))]
    (let [img (JPEGFactory/createFromStream doc image-stream)]
      (draw-imagex c-stream img x y width height))))

(defn draw-image! [doc page url x y width height]
  (let [lower-case-url (s/lower-case url)
        jpg? (or (s/ends-with? lower-case-url "jpg")
                 (s/ends-with? lower-case-url "jpeg"))
        draw-fn (if jpg? draw-jpg draw-non-jpg)]
    (try
      (draw-fn doc page url x y width height)
      (catch Exception e (prn "failed loading image" (clojure.stacktrace/print-stack-trace e))))))

(defn get-page [doc index]
  (.getPage doc index))

(defn string-width [text font font-size]
  (/ (* (/ (.getStringWidth font text) 1000.0) font-size) 72))

(defn split-lines [text font-size width]
  (let [words (s/split text #"\s")]
    (loop [lines []
           current-line nil
           [next-word & remaining-words :as current-words] words]
      (if next-word
        (let [line-with-word (str current-line (if current-line " ") next-word)
              new-width (string-width line-with-word PDType1Font/HELVETICA font-size)]
          (if (> new-width width)
            (recur (conj lines current-line)
                   nil
                   current-words)
            (recur lines
                   line-with-word
                   remaining-words)))
        (if current-line
          (conj lines current-line)
          lines)))))

(defn draw-lines-to-box [cs lines font font-size x y height]
  (let [leading (* font-size 1.1)
        max-lines (- (/ (* 72 height) leading) 1)
        units-x (* 72 x)
        units-y (* 72 y)
        fitting-lines (vec (take max-lines lines))]
    (.beginText cs)
    (.setFont cs font font-size)
    (.moveTextPositionByAmount cs units-x units-y)
    (doseq [i (range (count fitting-lines))]
      (let [line (get fitting-lines i)]
        (.moveTextPositionByAmount cs 0 (- leading))
        (.drawString cs line)))
    (.endText cs)
    (vec (drop max-lines lines))))

(defn draw-text-to-box [cs text font font-size x y width height]
  (let [lines (split-lines text font-size width)]
    (draw-lines-to-box cs lines font font-size x y height)))

(defn set-text-color [cs r g b]
  (.setNonStrokingColor cs r g b))

(defn draw-text [cs text font font-size x y & [color]]
  (let [units-x (* 72 x)
        units-y (* 72 y)]
    (.beginText cs)
    (.setFont cs font font-size)
    (if color
      (apply set-text-color cs color))
    (.moveTextPositionByAmount cs units-x units-y)
    (.drawString cs text)
    (if color
      (set-text-color cs 0 0 0))
    (.endText cs)))

(defn draw-line [cs start-x start-y end-x end-y]
  (.drawLine cs start-x start-y end-x end-y))

(defn inches-to-units [inches]
  (float (* inches 72)))

(defn draw-line-in [cs & coords]
  (apply draw-line cs (map inches-to-units coords)))

(defn draw-grid [cs box-width box-height]
  (let [num-boxes-x (int (/ 8.5 box-width))
        num-boxes-y (int (/ 11.0 box-height))
        total-width (* num-boxes-x box-width)
        total-height (* num-boxes-y box-height)
        remaining-width (- 8.5 total-width)
        margin-x (/ remaining-width 2)
        remaining-height (- 11.0 total-height)
        margin-y (/ remaining-height 2)]
    (.setStrokingColor cs 225 225 225)
    (doseq [i (range (inc num-boxes-x))]
      (let [x (+ margin-x (* box-width i))]
        (draw-line-in cs
                      x    
                      margin-y
                      x
                      (+ margin-y total-height))))
    (doseq [i (range (inc num-boxes-y))]
      (let [y (+ margin-y (* box-height i))]
        (draw-line-in cs
                      margin-x
                      y
                      (+ margin-x total-width)
                      y)))
    (.setStrokingColor cs 0 0 0)))
(defn spell-school-level [{:keys [level school]}]
  (if (zero? level)
    (str (s/capitalize school) " cantrip")
    (str "Level-" level " " school)))

(defn draw-spell-field [cs document title value x y]
  (with-open [img-stream (io/input-stream (io/resource (str "public/image/" title ".png")))]
    (draw-imagex cs
                 (LosslessFactory/createFromImage document (ImageIO/read img-stream))
                 x
                 (- 11 y 0.12)
                 0.25
                 0.25))
  (.setNonStrokingColor cs 0 0 0 225)
  (draw-text cs
             value
             PDType1Font/HELVETICA_BOLD
             8
             x
             (- y 0.05))
  (.setNonStrokingColor cs 0 0 0))

(defn abbreviate-times [time]
  (-> time
      (s/replace #"minute" "min")
      (s/replace #"hour" "hr")))

(defn max-len [s len]
  (if (<= (count s) len)
    s
    (subs s 0 len)))

(defn abbreviate-duration [duration]
  (-> duration
      (s/replace #"Concentration,? up to " "Conc, ")
      abbreviate-times
      (s/replace #"Instantaneous.*" "Inst")
      (s/replace #"round" "rnd")
      (max-len 16)))

(defn abbreviate-casting-time [casting-time]
  (-> casting-time
      abbreviate-times
      (s/replace #"bonus action" "b.a.")
      (s/replace #"action" "act.")
      (s/replace #"reaction" "react.")))

(defn abbreviate-range [range]
  (-> range
      (s/replace #"Self.*" "Self")
      (s/replace #"feet" "ft")))

(defn print-backs [cs document box-width box-height remaining-lines-vec]
  (let [num-boxes-x (int (/ 8.5 box-width))
        num-boxes-y (int (/ 11.0 box-height))
        total-width (* num-boxes-x box-width)
        total-height (* num-boxes-y box-height)
        remaining-width (- 8.5 total-width)
        margin-x (/ remaining-width 2)
        remaining-height (- 11.0 total-height)
        margin-y (/ remaining-height 2)]
    (with-open [img-stream (io/input-stream (io/resource "public/image/orcpub-card-logo.png"))
                over-img-stream (io/input-stream (io/resource "public/image/clockwise-rotation.png"))]
      (let [img (LosslessFactory/createFromImage document (ImageIO/read img-stream))
            over-img (LosslessFactory/createFromImage document (ImageIO/read over-img-stream))]
        (doall
         (for [i (range num-boxes-x)
               j (range num-boxes-y)]
           (let [x (+ margin-x (* box-width i))
                 y (+ margin-y (* box-height j))
                 spell-index (+ i (* j num-boxes-x))
                
                 {:keys [remaining-lines spell-name]} (remaining-lines-vec spell-index)]
             (draw-grid cs 2.5 3.5)
             (when (seq remaining-lines)
               (draw-text-to-box cs
                               spell-name
                               PDType1Font/HELVETICA_BOLD
                               10
                               (+ x 0.12)
                               (- 11.0 y 0.08)
                               (- box-width 0.3)
                               0.25)
               (draw-lines-to-box cs
                               remaining-lines
                               PDType1Font/HELVETICA
                               8
                               (+ x 0.12)
                               (- 11.0 y 0.24)
                               (- box-height 0.2))))))))))

(defn print-spells [cs document box-width box-height spells]
  (let [num-boxes-x (int (/ 8.5 box-width))
        num-boxes-y (int (/ 11.0 box-height))
        total-width (* num-boxes-x box-width)
        total-height (* num-boxes-y box-height)
        remaining-width (- 8.5 total-width)
        margin-x (/ remaining-width 2)
        remaining-height (- 11.0 total-height)
        margin-y (/ remaining-height 2)]
    (with-open [img-stream (io/input-stream (io/resource "public/image/orcpub-card-logo.png"))
                over-img-stream (io/input-stream (io/resource "public/image/clockwise-rotation.png"))]
      (let [img (LosslessFactory/createFromImage document (ImageIO/read img-stream))
            over-img (LosslessFactory/createFromImage document (ImageIO/read over-img-stream))]
        (doall
         (for [j (range num-boxes-y)
               i (range (dec num-boxes-x) -1 -1)
               :let [spell-index (+ i (* j num-boxes-x))]]
           (if-let [{:keys [class-nm dc attack-bonus spell] :as spell-data}
                    (get (vec spells) spell-index)]
             (if spell
               (do
                 (let [x (+ margin-x (* box-width i))
                       y (+ margin-y (* box-height j))

                       {:keys [page source description]} spell

                       dc-str (str "DC " dc)
                       dc-offset (+ x 0.22 (string-width class-nm PDType1Font/HELVETICA_BOLD 10))
                       remaining-desc-lines
                       (draw-text-to-box cs
                                         (or description
                                             (str "see "
                                                  (if source
                                                    (s/upper-case (name source))
                                                    "PHB")
                                                  " "
                                                  page))
                                         PDType1Font/HELVETICA
                                         8
                                         (+ x 0.12)
                                         (- 11.0 y 0.65)
                                         (- box-width 0.24)
                                         (- box-height 0.9))]
                   (draw-grid cs 2.5 3.5)
                   (draw-imagex cs
                                img
                                (+ x 1.4)
                                (+ y 0.05)
                                1.0
                                0.25)
                   (draw-text-to-box cs
                                     (spell-school-level spell)
                                     PDType1Font/HELVETICA_OBLIQUE
                                     8
                                     (+ x 0.12)
                                     (- 11.0 y 0.10)
                                     (- box-width 0.24)
                                     0.25)
                   (draw-text-to-box cs
                                     (:name spell)
                                     PDType1Font/HELVETICA_BOLD
                                     10
                                     (+ x 0.12)
                                     (- 11.0 y 0.27)
                                     (- box-width 0.3)
                                     0.25)
                   (draw-spell-field cs
                                     document
                                     "magic-swirl"
                                     (abbreviate-casting-time
                                      (first
                                       (s/split
                                        (:casting-time spell)
                                        #",")))
                                     (+ x 0.12)
                                     (- 11.0 y 0.55))
                   (draw-spell-field cs
                                     document
                                     "arrow-dunk"
                                     (abbreviate-range (:range spell))
                                     (+ x 0.62)
                                     (- 11.0 y 0.55))
                   (draw-spell-field cs
                                     document
                                     "shiny-purse"
                                     (s/join
                                      ","
                                      (remove
                                       nil?
                                       (map
                                        (fn [[k v]]
                                          (if (-> spell :components k)
                                            v))
                                        {:verbal "V"
                                         :somatic "S"
                                         :material "M"})))
                                     (+ x 1.12)
                                     (- 11.0 y 0.55))
                   (draw-spell-field cs
                                     document
                                     "sands-of-time"
                                     (abbreviate-duration (:duration spell))
                                     (+ x 1.62)
                                     (- 11.0 y 0.55))
                   (draw-text cs
                              class-nm
                              PDType1Font/HELVETICA_BOLD
                              10
                              (+ x 0.12)
                              (- 11.0 y 3.4)
                              [186 21 3])
                   (draw-text cs
                              dc-str
                              PDType1Font/HELVETICA_BOLD_OBLIQUE
                              8
                              dc-offset
                              (- 11.0 y 3.4)
                              [186 21 3])
                   (draw-text cs
                              (str "Atk " (common/bonus-str attack-bonus))
                              PDType1Font/HELVETICA_BOLD_OBLIQUE
                              8
                              (+ dc-offset (string-width dc-str PDType1Font/HELVETICA_BOLD 10))
                              (- 11.0 y 3.4)
                              [186 21 3])
                   (if (seq remaining-desc-lines)
                     (draw-imagex cs
                                  over-img
                                  (+ x 2.2)
                                  (+ y 3.2)
                                  0.25
                                  0.25))
                   {:remaining-lines remaining-desc-lines
                    :spell-name (:name spell)}))))))))))
