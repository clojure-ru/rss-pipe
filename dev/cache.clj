(ns cache
  (:require [mount.core :as mount]))

(mount/defstate code-cache
  :start (atom {})) ;; clojure.lang.PersistentList -> Any

(defmacro cache!
  "Cache some slow/io stuff in REPL
  This survives after (repl/refresh) but not (reset)
  Use like this:

  (defn slow-f [] (Thread/sleep 1000) (+ 1 1))

  (-> (slow-f) cache! aprint) ;; takes 1s
  (-> (slow-f) cache! aprint) ;; now immediately"
  [form]
  `(let [key# (quote ~form)]
     (if-let [cached# (get @code-cache key#)]
       cached#
       (let [res# ~form]
         (swap! code-cache assoc (quote ~form) res#)
         res#))))
