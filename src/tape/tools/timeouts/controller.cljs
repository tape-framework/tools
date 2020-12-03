(ns tape.tools.timeouts.controller
  (:refer-clojure :exclude [set])
  (:require [re-frame.core :as rf]
            [tape.mvc.controller :as c :include-macros true]))

;;; Effects

(defn ^{::c/fx ::set} set-fx [m]
  (let [{:keys [ms set timeout]} m
        id (js/setTimeout #(rf/dispatch timeout) ms)]
    (when set
      (rf/dispatch (conj set id)))))

(defn ^{::c/fx ::clear} clear-fx [id] (js/clearTimeout id))

;;; Events

(defn ^::c/event-fx set [_ [_ m]] {::set m})
(defn ^::c/event-fx clear [_ [_ id]] {::clear id})

;;; Module

(c/defmodule)
