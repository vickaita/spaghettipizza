(ns limn.models.app
  (:require [spaghetti-pizza.pizza :refer [fresh-pizza]]))

(def default-app-state
  {:debug true
   :image-url nil
   :image-loading? false
   :easel-width 0
   :easel-height 0
   :viewport-width 512
   :viewport-height 512
   :scale-by 1
   :granularity 5
   :pizza (fresh-pizza)
   :strokes []
   :show-toolbar? false
   :show-color-wheel? false
   :toolbar {:groups [#_{:name "Test"
                         :tools [{:id :edit :name "Edit"}]}
                      {:name "Pasta"
                       :tools [{:id :spaghetti :name "Spaghetti"}
                               {:id :linguini :name "Linguini"}
                               {:id :ziti :name "Ziti"}]}
                      {:name "Cheese"
                       :tools [{:id :ricotta :name "Ricotta"}]}
                      {:name "Herbs"
                       :tools [{:id :basil :name "Basil"}]}]
             :colors [{:name "Red" :fill "#F86969" :stroke "#F04F4F"}
                      {:name "Orange" :fill "#EF951B" :stroke "#B55F11"}
                      {:name "Yellow" :fill "#FAE265" :stroke "#DDAB0B"}
                      {:name "Green" :fill "#83D874" :stroke "#5FBA52"}
                      {:name "Blue" :fill "#6EB4E1" :stroke "#1E6B92"}
                      {:name "Purple" :fill "#D459A4" :stroke "#AE4173"}
                      {:name "White" :fill "#F2F2F2" :stroke "#D9D9D9"}
                      {:name "Black" :fill "#202020" :stroke "#181818"}]}
   :menu-bar [{:name "Tools"
               :items [{:name "Foo" :command "foo"}
                       {:name "Bar" :command "bar"}]}]
   :tool :spaghetti
   :color {:name "Yellow" :fill "#FAE265" :stroke "#DDAB0B"}})
