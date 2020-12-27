(ns tape.tools-test
  (:require [cljs.test :refer [deftest is use-fixtures]]
            [integrant.core :as ig]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [day8.re-frame.test :as rft]
            [tape.mvc.controller :as c :include-macros true]
            [tape.mvc.view :as v]
            [tape.module :as module :include-macros true]
            [tape.tools :as tools :include-macros true]
            [tape.tools.app.controller :as app.c]))

(module/load-hierarchy)

;;; Ergonomic API

(deftest dispatch-test
  (with-redefs [rf/dispatch identity]
    (is (= [::app.c/event-db]
           (tools/dispatch [app.c/event-db])))))

(deftest subscribe-test
  (with-redefs [rf/subscribe identity]
    (is (= [::app.c/sub]
           (tools/subscribe [app.c/sub])))))

;;; Lens

(defn field
  {::c/reg ::c/event-db}
  [db [_ k v]] (assoc-in db [::todo k] v))

(defn todo
  {::c/reg ::c/sub}
  [db _] (::todo db))

(c/defmodule)

(def ^:private config
  {:tape.profile/base {}
   ::c/module         nil
   ::v/module         nil
   ::module           nil})

(def ^:private system nil)

(use-fixtures :once
  {:before (fn [] (set! system (-> config module/prep-config ig/init)))
   :after  (fn [] (ig/halt! system))})

(deftest lens*-test
  (rft/run-test-sync
   (let [lens   (tools/lens* ::todo ::field)
         cursor (r/cursor lens [:done])]
     (reset! cursor false)
     (is (false? @cursor))
     (swap! cursor not)
     (is (true? @cursor)))))

(deftest lens-test
  (rft/run-test-sync
   (let [lens   (tools/lens todo field)
         cursor (r/cursor lens [:done])]
     (reset! cursor false)
     (is (false? @cursor))
     (swap! cursor not)
     (is (true? @cursor)))))
