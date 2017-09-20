(defproject rss-pipe "0.1.0-SNAPSHOT"
  :description "Filter/reduce your rss feed"
  :url "https://github.com/clojure-ru/rss-feed"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta1"]
                 ;; [org.clojure/test.check "0.10.0-alpha2"]
                 [mount "0.1.11"]
                 [cprop "0.1.10"]
                 [clj-http "3.7.0"]
                 [org.clojure/core.async "0.3.443"]]
  :plugins [[lein-ancient "0.6.10" :exclusions [org.clojure/clojure]]
            [lein-cloverage "1.0.9" :exclusions [org.clojure/clojure]]
            [lein-cljfmt "0.5.6" :exclusions [org.clojure/clojure]]
            [jonase/eastwood "0.2.3" :exclusions [org.clojure/clojure]]]
  :aliases {"lint" ["do"
                    ["cljfmt" "check"]
                    ["eastwood"]]}
  :main ^:skip-aot rss-pipe.core
  :target-path "target/%s"
  :exclusions [org.clojure/clojure
               org.clojure/clojurescript]
  :profiles {:dev     {:main           ^:replace dev
                       :resource-paths ["dev-resources"]
                       :source-paths   ["dev"]
                       :dependencies   [[pjstadig/humane-test-output "0.8.2"]
                                        [com.rpl/specter "1.0.2"]
                                        [org.clojure/tools.namespace "0.2.11"]
                                        [aprint "0.1.3"]
                                        [slamhound "1.5.5"]
                                        [org.slf4j/jcl-over-slf4j "1.7.22"]
                                        [org.slf4j/slf4j-api "1.7.22"]
                                        [com.cemerick/pomegranate "0.4.0" :exclusions [org.slf4j/jcl-over-slf4j
                                                                                       org.slf4j/slf4j-api]]
                                        [im.chit/lucid.mind "1.3.13"]]
                       :injections     [(require 'pjstadig.humane-test-output)
                                        (pjstadig.humane-test-output/activate!)]}
             :uberjar {:main     ^:replace rss-pipe.core.main
                       :aot      :all}
             :test    {:dependencies [[pjstadig/humane-test-output "0.8.2"]]
                       :injections   [(require 'pjstadig.humane-test-output)
                                      (pjstadig.humane-test-output/activate!)]}})
