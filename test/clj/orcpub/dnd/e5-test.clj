(ns orcpub.dnd.e5-test
  (:require [clojure.test :refer :all]
            [orcpub.dnd.e5 :as e5]
            [clojure.spec.alpha :as spec]))

(def plugins-1 {"XGE" {:orcpub.dnd.e5/backgrounds {:bg1 {:x 1
                                                         :option-pack "XGE"}
                                                   :bg2 {:x 1
                                                         :option-pack "XGE"}}}
                "VOLO" {:orcpub.dnd.e5/classes {:c1 {:x 1
                                                     :option-pack "VOLO"}
                                                :c2 {:x 1
                                                     :option-pack "VOLO"}}}})

(deftest test-specs
  (is (spec/valid? ::e5/plugins plugins-1))
  (is (not (spec/valid? ::e5/plugin plugins-1)))
  (is (spec/valid? ::e5/plugin (plugins-1 "XGE")))
  (is (not (spec/valid? ::e5/plugins (plugins-1 "XGE")))))

(deftest test-merge-all-plugins
  (let [plugins-2 {"XGE" {:orcpub.dnd.e5/backgrounds {:bg2 {:x 2
                                                            :option-pack "XGE"}
                                                      :bg3 {:x 2
                                                            :option-pack "XGE"}}}
                   "PHB" {:orcpub.dnd.e5/classes {:c1 {:x 2
                                                       :option-pack "PHB"}
                                                  :c2 {:x 2
                                                       :option-pack "PHB"}}}}
        expected-result {"XGE" {:orcpub.dnd.e5/backgrounds {:bg1 {:x 1
                                                                  :option-pack "XGE"}
                                                            :bg2 {:x 2
                                                                  :option-pack "XGE"}
                                                            :bg3 {:x 2
                                                                  :option-pack "XGE"}}}
                         "VOLO" {:orcpub.dnd.e5/classes {:c1 {:x 1
                                                              :option-pack "VOLO"}
                                                         :c2 {:x 1
                                                              :option-pack "VOLO"}}}
                         "PHB" {:orcpub.dnd.e5/classes {:c1 {:x 2
                                                             :option-pack "PHB"}
                                                        :c2 {:x 2
                                                             :option-pack "PHB"}}}}]
    (is (= expected-result (e5/merge-all-plugins plugins-1 plugins-2)))
    (is (= plugins-1 (e5/merge-all-plugins plugins-1 plugins-1)))))
