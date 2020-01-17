(ns tape.tools
  (:require [re-frame.core :as rf]))

(defn lens
  "Creates a lens that reads from a subscription and writes by dispatching an
  event. Ex: `(lens ::todos.c/todo ::todos.c/field)`."
  ([k] (lens k k))
  ([kget kset]
   (fn
     ([ks] (get-in @(rf/subscribe [kget]) ks))
     ([ks v] (rf/dispatch (conj (into [kset] ks) v))))))

(defn- when-valid* [ev f]
  (let [form (-> ev .-target .-form)]
    (.preventDefault ev)
    (if (.checkValidity form)
      (f)
      (.reportValidity form))))

(defn when-valid
  "Wraps the event handler `f` and calls it only if HTML5 Validation API is true
  on the form corresponding to the event. Ex:
  `[:button {:on-click (when-valid #(rf/dispatch [::save]))} \"Save\"]`."
  [f] #(when-valid* % f))
