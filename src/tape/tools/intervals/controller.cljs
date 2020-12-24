(ns tape.tools.intervals.controller
  (:refer-clojure :exclude [set])
  (:require [re-frame.core :as rf]
            [tape.mvc.controller :as c :include-macros true]))

;;; Effects

(defn set-fx
  {::c/reg ::c/fx
   ::c/id ::set}
  [ainterval]
  (let [{:keys [ms set interval]} ainterval
        interval-id (js/setInterval #(rf/dispatch interval) ms)]
    (when set
      (rf/dispatch (conj set interval-id)))))

(defn clear-fx
  {::c/reg ::c/fx
   ::c/id ::clear}
  [interval-id] (js/clearInterval interval-id))

;;; Events

(defn set
  {::c/reg ::c/event-fx}
  [_ [_ interval]] {::set interval})

(defn clear
  {::c/reg ::c/event-fx}
  [_ [_ interval-id]] {::clear interval-id})

;;; Module

(c/defmodule)
