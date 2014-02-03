(ns pizza.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [goog.history.Html5History]
            [cljs.core.async :refer [put! close! chan <! map<]]
            [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]
            [pizza.toolbar :as toolbar]
            [pizza.easel :as easel]
            [pizza.command :refer [exec]]))

(enable-console-print!)

(defn- get-pizza-hash
  []
  ;; TODO: Can I get this information straight from the "navigation" event and
  ;; eliminate this function entirely?
  (-> (.-search (.-location js/document))
      (.split "=")
      (aget 1)))

(defn app-view
  [app owner]
  (om/component
    (html
      [:div#site {:class (when (:show-toolbar? app) "show-toolbar")}
       (om/build toolbar/toolbar
                 (om/graft {:commands (:commands app)
                            :tool (:tool app)
                            :groups (:groups (:toolbar app))}
                           app))
       [:div#page
        [:header#masthead
         [:a#menu-control
          {:on-click (fn [e] (doto e .preventDefault .stopPropagation)
                       (om/transact! app [:show-toolbar?] not))}]
         [:h1 "Spaghetti Pizza"]]
        (om/build easel/easel app)]
       [:footer [:p "Created by Vick Aita"]]])))

(defn -main
  []
  (let [;; TODO: goog.history.Html5History is pretty annoying and doesn't fit
        ;; very well with a functional style. I should either use the native
        ;; HTML5 methods or wrap it.
        history
        (goog.history.Html5History.
          js/window
          (let [tt #js {}]
            (set! (.-createUrl tt)
                  (fn [token _ _] token))
            (set! (.-retrieveToken tt)
                  (fn [path-prefix location]
                    (.substr (.-pathname location)
                             (count path-prefix)))) tt)
          ;; XXX: For some reason this tagged literal is breaking in
          ;; advanced compilation. That's why I'm doing that crazy let
          ;; block above. Honestly, I don't know why this isn't working.
          ;#js {:createUrl (fn [token _ _] token)
          ;     :retrieveToken (fn [path-prefix location]
          ;                      (.substr (.-pathname location)
          ;                               (count path-prefix)))}
          )

        commands
        (chan)

        app-state
        (atom {:commands commands
               :debug false
               :image-url nil
               :image {:url nil :status :empty}
               :width 512
               :height 512
               :granularity 5
               :pizza {:crust (easel/create-irregular-circle [256 256] 227)
                       :sauce (easel/create-irregular-circle [256 256] 210)}
               :strokes []
               :show-toolbar? false
               :toolbar {:groups [{:name "Pasta"
                                   :tools [{:id :spaghetti :name "Spaghetti"}
                                           {:id :linguini :name "Linguini"}
                                           {:id :ziti :name "Ziti"}]}
                                  {:name "Cheese"
                                   :tools [{:id :ricotta :name "Ricotta"}]}
                                  {:name "Edit"
                                   :tools [{:id :edit :name "Edit"}]}]}
               :tool :spaghetti})]

    (doto history
      (events/listen "navigate"
                  #(swap! app-state assoc
                          :image-url
                          (let [pizza-hash (get-pizza-hash)]
                            (when (> (count pizza-hash) 0)
                              (str "/pizza/" (get-pizza-hash) ".png")))))
      (.setUseFragment false)
      (.setEnabled true))

    ;;;;; TODO: This should also be fired whenever the viewport is resized.
    ;;;#_(easel/adjust-size! svg-elem)

    ;;;;; XXX: This seems to be breaking noodle drawing on mobile, so disabled
    ;;;;; until I have time to investigate.
    ;;;#_(events/listen (dom/getElement "page") "click"
    ;;;              #(when (toolbar/visible?)
    ;;;                 (do (.preventDefault %)
    ;;;                     (.stopPropagation %)
    ;;;                     (toolbar/hide!))))

    (go (while true
          (let [command (<! commands)]
            (when (:debug @app-state) (prn command))
            (swap! app-state exec command))))

    (om/root app-state app-view (.-body js/document))))

(events/listen js/document "DOMContentLoaded" -main)
#_(repl/connect "http://ui:9000/repl")
