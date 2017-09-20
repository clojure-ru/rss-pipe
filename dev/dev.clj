(ns dev
  (:require [aprint.core :refer [aprint ap]]
            [cache :refer [cache!]]
            [clojure.test :as test]
            [feedparser-clj.core :as feedparser]
            [clojure.repl :refer [source]]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.tools.namespace.repl :as repl]
            [clojure.walk :refer [keywordize-keys stringify-keys]]
            [slam.hound :refer [reconstruct swap-in-reconstructed-ns-form]]
            [cemerick.pomegranate :as pomegranate]
            [lucid.mind :refer :all]
            [mount.core :as mount]
            [digest]
            [clojure.core.async :as async :refer [chan >! >!! <!! <! go go-loop]]
            [rss-pipe.core :as rss-pipe]
            [rss-pipe.config :as config]))

(alter-var-root #'aprint.dispatch/*aprint-seq-length* (constantly 1000))

(defn clsp!
  "Adds dependency to classpath. Usage: (clsp! '[[dep \"RELEASE\"]])"
  [x]
  (pomegranate/add-dependencies :coordinates x
                                :repositories (merge cemerick.pomegranate.aether/maven-central
                                                     {"clojars" "https://clojars.org/repo"})))

(defmacro adef
  "Same def, but immediately print the value"
  [x & body]
  `(do
     (def ~x (do ~@body))
     (aprint ~x)))

(defmacro ap->
  "This assigns result of expr to a dynamic var
  Use instead of
  (-> ... fn1 aprint)
  Use
  (ap-> ... fn1)
  This will pretty print result and save it to a var like fn1-1111"
  [& body]
  (let [id-form (last body)
        id-form (if (sequential? id-form) "x" id-form)]
    `(let [res# (-> ~@body)]
       (aprint res#)
       (def ~(symbol (str id-form "-" (System/currentTimeMillis))) res#))))

(defn start
  []
  (mount/start))

(defn stop
  []
  (mount/stop))

(defn reset
  []
  (stop)
  (repl/refresh :after 'dev/start))

(mount/in-clj-mode)

(comment
  ;; repl-based workflow encouraged:
  ;; - run some code in repl until satisfaction. use cache! macro
  ;; - put code into code ns
  ;; - (repl/refresh)
  ;; - or instead after config change etc: (reset)
  ;; - TODO: run linters and autofix code: $ lein cljfmt fix from repl
  ;; - run tests
  ;; - repeat

  ;; start with state initializations
  (reset)

  ;; get the feed
  (ap-> config/config :github-rss feedparser/parse-feed cache! keys)
  ;; feed without entries
  (adef kk (remove #{:entries} keys-1505887051677)) ;; fix the name from prev result, or  (adef kk (remove #{:entries} (var-get *1)))
  ;; leverage cache and some state
  (ap-> config/config :github-rss feedparser/parse-feed cache! (select-keys kk))

  ;; tests
  (test/run-all-tests))
