(ns dev
  (:require [aprint.core :refer [aprint ap]]
            [clojure.repl :refer [source]]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.tools.namespace.repl :as repl]
            [clojure.walk :refer [keywordize-keys stringify-keys]]
            [slam.hound :refer [reconstruct swap-in-reconstructed-ns-form]]
            [cemerick.pomegranate :as pomegranate]
            [lucid.mind :refer :all]
            [mount.core :as mount]
            [clojure.core.async :as async :refer [chan >! >!! <!! <! go go-loop]]
            [rss-pipe.core :as rss-pipe]
            [rss-pipe.config :as config]))

(alter-var-root #'aprint.dispatch/*aprint-seq-length* (constantly 1000))

(defn clsp!
  "Adds dependency to classpath. Usage: (clsp! '[[dep \"RELEASE\"]])"
  [x]
  (pomegranate/add-dependencies :coordinates x
                                :repositories (merge cemerick.pomegranate.aether/maven-central
                                                     {"clojars" "http://clojars.org/repo"})))

(defmacro adef
  "Same def, but immediately print the value"
  [x & body]
  `(do
     (def ~x ~@body)
     (aprint ~x)))

(defn start [] (mount/start))
(defn stop [] (mount/stop))
(defn reset [] (stop) (repl/refresh :after 'dev/start))
(mount/in-clj-mode)

(comment
  ;; get the feed
  ;; tests
  (reset) (clojure.test/run-all-tests)
  )
