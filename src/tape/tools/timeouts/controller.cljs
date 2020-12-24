(ns tape.tools.timeouts.controller
  (:refer-clojure :exclude [set])
  (:require [re-frame.core :as rf]
            [tape.mvc.controller :as c :include-macros true]))

;;; Effects

(defn set-fx
  {::c/reg ::c/fx
   ::c/id ::set}
  [atimeout]
  (let [{:keys [ms set timeout]} atimeout
        timeout-id (js/setTimeout #(rf/dispatch timeout) ms)]
    (when set
      (rf/dispatch (conj set timeout-id)))))

(defn clear-fx
  {::c/reg ::c/fx
   ::c/id ::clear}
  [timeout-id] (js/clearTimeout timeout-id))

;;; Events

(defn set
  {::c/reg ::c/event-fx}
  [_ [_ timeout]] {::set timeout})

(defn clear
  {::c/reg ::c/event-fx}
  [_ [_ timeout-id]] {::clear timeout-id})

;;; Module

(c/defmodule)
