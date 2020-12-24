(ns tape.tools.timeouts.controller-test
  (:refer-clojure :exclude [set])
  (:require [cljs.test :refer [deftest is use-fixtures]]
            [day8.re-frame.test :as rft]
            [integrant.core :as ig]
            [re-frame.core :as rf]
            [tape.mvc.controller :as c :include-macros true]
            [tape.mvc.view :as v]
            [tape.module :as module :include-macros true]
            [tape.tools.timeouts.controller :as timeouts.c]))

(module/load-hierarchy)

(defn set
  {::c/reg ::c/event-db}
  [db [_ id]] (assoc db ::timeout-id id))

(defn timeout
  {::c/reg ::c/event-db}
  [db [_]] (assoc db ::timeout-done true))

(defn timeout-id
  {::c/reg ::c/sub}
  [db _] (::timeout-id db))

(defn timeout-done
  {::c/reg ::c/sub}
  [db _] (::timeout-done db))

(c/defmodule)

(def ^:private config
  {:tape.profile/base  {}
   ::c/module          nil
   ::v/module          nil
   ::timeouts.c/module nil
   ::module            nil})

(def ^:private system nil)

(use-fixtures :once
  {:before (fn [] (set! system (-> config module/prep-config ig/init)))
   :after  (fn [] (ig/halt! system))})

(deftest set-test
  (rft/run-test-async
   (let [timeout    {:ms 1, :set [::set], :timeout [::timeout]}
         timeout-id (rf/subscribe [::timeout-id])
         done       (rf/subscribe [::timeout-done])]
     (rf/dispatch [::timeouts.c/set timeout])
     (rft/wait-for [::timeout]
       (is (integer? @timeout-id))
       (is (true? @done))))))

(deftest clear-test
  (rft/run-test-sync
   (let [timeout {:ms      1
                  :set     [::timeouts.c/clear]
                  :timeout [::does-not-dispatch]}
         id      (atom nil)]
     (with-redefs [js/setTimeout   (constantly 42)
                   js/clearTimeout #(reset! id %)]
       (rf/dispatch [::timeouts.c/set timeout])
       (is (= 42 @id))))))
