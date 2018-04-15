# orcpub

This is the code for OrcPub2.com. Many, many people have expressed interest in helping out or checking out the code, so I have decided to make that possible by open sourcing it.

## Getting Started with Development

- Install Java: http://openjdk.java.net/ or http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
- Install leiningen: https://leiningen.org/
- run `lein figwheel`

That should get a basic dev environment going and open your browser at [localhost:3449](http://localhost:3449/).
When you save changes, it will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

```clojure
(js/alert "Am I connected?")
```

and you should see an alert in the browser window.

Before you start up the back-end server, you will need to [set up Datomic locally](https://docs.datomic.com/on-prem/dev-setup.html). If you're just trying to get started quickly to contribute to the main project, and happen to be on macOS, you can use [homebrew](https://brew.sh/) to do this pretty quickly:

```
brew install datomic
brew services start datomic
```

You will then need to transact the schema. First start a REPL:

```
lein repl
```

Or if you are using Emacs with [Cider](https://cider.readthedocs.io/en/latest/) you can run the command to start the Cider REPL:

```
C-c M-j
```

For Vim users, [vim-fireplace](https://github.com/tpope/vim-fireplace) provides a good way to interact with a running repl without leaving Vim.

I haven't used [Cursive](https://cursive-ide.com/), but I hear it is really nice and I'm sure there's an easy way to start a REPL within it.

Once you have a REPL you can run this from within it to create the database, transact the database schema, and start the server:

```clojure
user=> (init-database)
user=> (start-server)
```

To stop you will need to do this:

```clojure
user=> (stop-server)
```

Within Emacs you should be able to save your file (C-x C-s) and reload it into the REPL (C-c C-w) to get your server-side changes to take effect. Within Vim with `vim-fireplace` you can eval a form with `cpp`, a paragraph with `cpip`, etc; check out its help file for more information. Regardless of editor, your client-side changes will take effect immediately when you change a CLJS or CLJC file while `lein figwheel` is running.

## OrcPub Fundamentals

### Overview

OrcPub's design is based around the concept of hierarchical option selections applying modifiers to a entity. Consider D&D 5e as an example. In D&D 5e you build and maintain characters, which are entities, by selecting from a set of character options, such as race and class. When you select a race you will be affored other option selections, such as subrace or subclass. Option selections also apply modifiers to your character, such as 'Darkvision 60'. Option selections are defined in templates. An entity is really just a record of hierarchical choices made. A built entity is a collection of derived attributes and functions derived from applying all of the modifiers of all the choices made. Here is some pseudocode to this more concrete:

```clojure
user> (def character-entity {:options {:race
                                       {:key :elf,
                                        :options {:subrace {:key :high-elf}}}}})
                                          
user> (def template {:selections [{:key :race
                                   :min 1
                                   :max 1
                                   :options [{:name "Elf"
                                              :key :elf
                                              :modifiers [(modifier ?dex-bonus (+ ?dex-bonus 2))
                                                          (modifier ?race "Elf")]
                                              :selections [{:key :subrace
                                                            :min 1
                                                            :max 1
                                                            :options [{:name "High Elf"
                                                                       :key :high-elf
                                                                       :modifiers [(modifier ?subrace "High Elf")
                                                                                   (modifier ?int-bonus (+ ?int-bonus 1))]}]}]}]}]}
                                                                 
user> (def built-character (build-entity charater-entity template))

user> built-character
{:race "Elf"
 :subrace "High Elf"
 :dex-bonus 2
 :int-bonus 1}
```

This may seem overly complicated, but after my work on the (Original Orcpub)[orcpub.com], I realized that this really the only real correct solution as it models how character building actually works. The original Orcpub stored characters essentially like the built-character above with a centralized set of functions to compute other derived values. This is the most straightforward solution, but this has flaws:

* You have difficulty figuring out which options have been selected and which ones still need to be selected.
* You keep having to patch your data as your application evolves. For example, say you store a character's known spells as a list of spell IDs. Then you realize later that users want to also know what their attack bonus is for each spell. At the very least you'll have to make some significant changes to every stored character.
* It is not scalable. Every time you add in more options, say from some new sourcebook, you have to pile on more conditional logic in your derived attribute functions. Believe me, this gets unmanageable very quickly.
* It's not reusable in, say, a Rifts character builder.

The OrcPub2 architecture fixes these problems:

* You know exactly which options have been selected, which have not, and how every modifier is arrived at, given the entity and the most up-to-date templates.
* You don't need to patch up stored characters if you find a bug since characters are stored as just a set of very generic choices.
* It's scalable and pluggable. Most logic for derived values is stored inside of the options that create or modify these derived values. Many rules within D&D 5e allow for picking the best of alternate calculations for a particular attribute (AC comes to mind). With the OrcPub2 solution you can have an option easily override or modify a calculation without any other code having to know about it. This makes for a MUCH more manageable solution and makes it easy to add options as plugins. 
* The entity builder engine could be used for building any character in any system. In fact, it could be used to build any entity in any system. For example, say you have a game system with well-defined mechanics building a vehicle, the entity builder engine could do that.


## FAQs
**Q: Ummmmm, why is your code so ugly, I thought Clojure code was supposed to be pretty.** 

**A:** *Yeah, about that...I worked on this for about 4 months full time, trying to compete with D&D Beyond's huge team and budget. That lead to a stressed-out me and ugly code. Help me make it pretty sucka!*


**Q: Mwahahahaha, now that I have your code I'm going to fork it and build the most awesome website in the world that will totally fucking annihilate OrcPub2.com. I'm going to call it FlumphTavern69.com. Come at me bro!**

**A:** *Motherfucking hell yeah, do that shit, flumphs are some sexy bitches!*


**Q: Blahahahaha, you done fucked up, we are super-mega-corp Hex Inc. we will steal your awesome code and put it into our less awesome app. What you got to say about that, huh, bitch?**

**A:** *I'm down for that shit, your app makes me sad, if you were to combine your official license and professional visual design with my more modern technical and UX design, your app would make me happy and I could justify paying all the money for all the content* 


**Q: Seriously?!!! Your unit test coverage is pathetic!**

**A:** *Yep, add some, it would be rad.*


**Q: I'm a newb Clojure developer looking to get my feet wet, where to start?**

**A:** *First I would start by getting the fundamentals down at http://www.4clojure.com/, then maybe getting your bearing by checking out my more gentle (and clean) introduction to the OrcPub stack: https://github.com/larrychristensen/messenjer, which I walkthrough on https://lambdastew.com. From there you might add some unit tests or pick up an open issue on the "Issues" tab (and add unit tests with it).*


**Q: Your DSL for defining character options is pretty cool, I can build any type of character option out there. How about I add a bunch on content from the Player's Handbook?**

**A:** *I love your enthusiasm, but we cannot accept pull requests containing copyrighted content. We do, however, encourage you to fork OrcPub and create your own private version with the full content options*
