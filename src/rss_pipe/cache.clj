(ns rss-pipe.cache
  (:refer-clojure :exclude [slurp])
  (:require [mount.core :as mount]
            [clojure.core.cache :as cache]))

(mount/defstate cache
  :start (atom (cache/ttl-cache-factory {} :ttl (* 60 10 1000))))

(defn fetch [key f]
  (cache/lookup (swap! cache
                       #(if (cache/has? % key)
                          (cache/hit % key)
                          (cache/miss % key (f key))))
                key))

(defn slurp [url]
  (fetch url clojure.core/slurp))
