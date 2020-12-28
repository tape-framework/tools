(ns tape.tools.timeouts.controller-test
  (:require [cljs.test :refer [deftest testing is]]
            [day8.re-frame.test :as rft]
            [integrant.core :as ig]
            [re-frame.core :as rf]
            [tape.mvc :as mvc :include-macros true]
            [tape.module :as module :include-macros true]
            [tape.tools.timeouts.controller :as timeouts.c]
            [tape.tools.app.controller :as app.c]))

(def ^:private config
  {::mvc/module nil
   ::timeouts.c/module nil})

(deftest timeout-test
  (let [system (-> config module/prep-config ig/init)]
    (testing "set"
      (rft/run-test-async
       (let [timeout {:ms 1
                      :set [::app.c/set-timeout]
                      :timeout [::app.c/timeout]}
             timeout-id (rf/subscribe [::app.c/timeout-id])
             done (rf/subscribe [::app.c/timeout-done])]
         (rf/dispatch [::timeouts.c/set timeout])
         (rft/wait-for [::app.c/timeout]
           (is (integer? @timeout-id))
           (is (true? @done))))))

    (testing "clear"
      (rft/run-test-sync
       (let [timeout {:ms 1
                      :set [::timeouts.c/clear]
                      :timeout [::does-not-dispatch]}
             id (atom nil)]
         (with-redefs [js/setTimeout (constantly 42)
                       js/clearTimeout #(reset! id %)]
           (rf/dispatch [::timeouts.c/set timeout])
           (is (= 42 @id))))))
    (ig/halt! system)))
