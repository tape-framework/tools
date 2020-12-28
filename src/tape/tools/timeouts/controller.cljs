(ns tape.tools.timeouts.controller
  (:refer-clojure :exclude [set])
  (:require [re-frame.core :as rf]
            [tape.mvc :as mvc :include-macros true]))

;;; Effects

(defn set-fx
  {::mvc/reg ::mvc/fx
   ::mvc/id ::set}
  [atimeout]
  (let [{:keys [ms set timeout]} atimeout
        timeout-id (js/setTimeout #(rf/dispatch timeout) ms)]
    (when set
      (rf/dispatch (conj set timeout-id)))))

(defn clear-fx
  {::mvc/reg ::mvc/fx
   ::mvc/id ::clear}
  [timeout-id] (js/clearTimeout timeout-id))

;;; Events

(defn set
  {::mvc/reg ::mvc/event-fx}
  [_ [_ timeout]] {::set timeout})

(defn clear
  {::mvc/reg ::mvc/event-fx}
  [_ [_ timeout-id]] {::clear timeout-id})

;;; Module

(mvc/defm ::module)
