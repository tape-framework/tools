(ns tape.tools
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn lens
  "Creates a lens that reads from a subscription and writes by dispatching an
  event. Ex: `(lens ::posts.c/post ::posts.c/field)`."
  ([k] (lens k k))
  ([kget kset]
   (fn
     ([ks] (get-in @(rf/subscribe [kget]) ks))
     ([ks v] (rf/dispatch (conj (into [kset] ks) v))))))

(defn cursor
  ([k] (lens k k))
  ([kget kset]
   (r/cursor (lens kget kset) [])))
