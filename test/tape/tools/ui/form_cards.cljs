(ns tape.tools.ui.form-cards
  (:require [reagent.core :as r]
            [devcards.core :refer-macros [defcard-rg]]
            [tape.tools.ui.form :as form]))

;;; Input tag

(def ^:private options {:man "Man", :woman "Woman"})

(defn- input-tag [m]
  [:div.field
   [:div.control
    [form/input-tag m]]])

(defn- select-tag [m]
  [:div.field
   [:div.control
    [:div.select {:class (when (:multiple m) "is-multiple")}
     [form/input-tag (merge {:type :select, :options options} m)]]]])

(defcard-rg input-tag-card
  [:div.columns
   [:div.column
    [input-tag {:type :text, :class "input"}]
    [select-tag {:include-blank true}]
    [select-tag {}]]
   [:div.column
    [input-tag {:type :textarea, :class "textarea"}]]
   [:div.column
    [:div.field
     [:div.control
      [form/input-tag {:type :checkbox, :name "checkbox-tag"}]
      [form/input-tag {:type :radio, :name "radio-tag"}]
      [form/input-tag {:type :radio, :name "radio-tag"}]]]]
   [:div.column
    [select-tag {:multiple true, :include-blank true}]]
   [:div.column
    [select-tag {:multiple true}]]])

;;; Input tag

(defn- input [m]
  (let [entity (r/atom {:field nil})]
    (fn [m]
      [:div.columns
       [:div.column
        [:div.field
         [:div.control
          [form/field (merge {:state entity :field :field} m)]]]]
       [:div.column [:pre (pr-str @entity)]]])))

(defn- radio []
  (let [entity (r/atom {:field nil})
        opts   {:state entity, :field :field, :type :radio, :name "radio"}]
    (fn []
      [:div.columns
       [:div.column
        [:div.field
         [:div.control
          [form/field (merge opts {:value "x"})]
          [form/field (merge opts {:value "y"})]]]]
       [:div.column [:pre (pr-str @entity)]]])))

(defn- select [m]
  (let [entity   (r/atom {:field nil})
        defaults {:type    :select
                  :options options
                  :state   entity
                  :field   :field}]
    (fn [m]
      [:div.columns
       [:div.column
        [:div.field
         [:div.control
          [:div.select {:class (when (:multiple m) "is-multiple")}
           [form/input (merge defaults m)]]]]]
       [:div.column [:pre (pr-str @entity)]]])))

(defcard-rg input-card
  [:div
   [input {:type :text, :class "input"}]
   [input {:type :textarea, :class "textarea"}]
   [input {:type :checkbox, :name "checkbox", :value "x"}]
   [radio]
   [select {:include-blank true}]
   [select {}]
   [select {:multiple true, :include-blank true}]
   [select {:multiple true}]])

;;; Constraint validation

(defcard-rg constraint-validation-card
  [:form
   [:div.field.has-addons
    [:div.control
     [:input.input {:type "text", :required true}]]
    [:div.control
     [:button.button.is-primary {:on-click (form/when-valid #(console.log ::valid))}
      "Submit"]]]])
