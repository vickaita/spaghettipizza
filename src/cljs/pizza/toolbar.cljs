(ns pizza.toolbar
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [cljs.core.async :refer [put! close! chan <! map<]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]
            [vickaita.channels :as ch]
            [pizza.svg :as svg]))

(defn toolbar
  [menu owner]
  (om/component
    (html
      [:section#toolbar.toolbar
       [:section.actions
        [:a#clear.action {:on-click #(put! (:cmd @menu) [:clear])} "Clear"]
        [:a#save.action {:on-click #(put! (:cmd @menu) [:save])} "Save"]]
       [:section.toppings
        [:h1 "Toppings!"]
        [:dl.tools
         (for [group (:groups menu)]
           (cons [:dt.group {:key (:name group)} (:name group)]
                 (for [tool (:tools group)]
                   [:dd [:a.tool {:class (when (= (:id tool) (:tool menu)) "active")
                                  :data-tool (:id tool)
                                  :key (:id tool)}
                         (:name tool)]])))]]])))

(defn toolbar*
  [app owner]
  (om/component
    (html [:div#toolbar.toolbar
           [:div.actions
            [:a#clean.action "Reset"]
            [:a#photo.action
             ; #_{:on-click
             ;                    (fn [e]
             ;                      (doto e .preventDefault .stopPropagation)
             ;                      (om/update! app assoc :show-toolbar? false)
             ;                      (let [api-url (if (= "spaghettipizza.us" (.-host (.-location js/document)))
             ;                                      "http://api.spaghettipizza.us/pizza/"
             ;                                      "/pizza/")
             ;                            blob (<! (svg/svg->img-chan svg-elem 612 612))
             ;                            data (doto (js/FormData.) (.append "data" blob))
             ;                            {fh :file-hash} (reader/read-string (<! (ch/xhr api-url "POST" data)))
             ;                            page-url (str #_(.-origin js/location) "?pizza=" fh)]
             ;                        (om/transact! app :image-url page-url)))}
             "Share"]]
           [:h2 "Toppings!"]
           (for [group app]
             [:dl.tools
              [:dt.group (:group-name group)]
              (for [tool (:tools group)]
                [:dd [:a.tool {:class (when (= (:id tool) (:tool @app)) "active")
                               :data-tool (:id tool)}
                      (:name tool)]])])])))
