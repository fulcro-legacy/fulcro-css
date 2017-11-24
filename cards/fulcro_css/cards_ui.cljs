(ns fulcro-css.cards-ui
  (:require [fulcro.client.dom :as dom]
            [devcards.core :as dc]
            [fulcro-css.css :as css]
            [fulcro.client.cards :refer [defcard-fulcro]]
            [fulcro.client.primitives :as prim :refer [defui]]))

(defui ListItem
  static css/CSS
  (local-rules [this] [[:.item {:color "red" :text-decoration "underline"}]])
  (include-children [this] [])
  Object
  (render [this]
    (let [{:keys [label]} (prim/props this)
          {:keys [item]} (css/get-classnames ListItem)]
      (dom/li #js {:className item} label))))

(def ui-list-item (prim/factory ListItem {:keyfn :id}))

(defui ListComponent
  static css/CSS
  (local-rules [this] [[:.items-wrapper {:color "blue"}]])
  (include-children [this] [ListItem])
  Object
  (render [this]
    (let [{:keys [id items]} (prim/props this)
          {:keys [items-wrapper]} (css/get-classnames ListComponent)]
      (dom/div #js {:className items-wrapper}
        (dom/h2 nil (str "List " id))
        (dom/ul nil (map ui-list-item items))))))

(def ui-list (prim/factory ListComponent {:keyfn :id}))

(defui Root
  static css/CSS
  (local-rules [this] [[:.container {:background-color "red"}]])
  (include-children [this] [ListComponent])
  static css/Global
  (global-rules [this] [[:.text {:color "yellow"}]])
  Object
  (render [this]
    (let [{:keys [text]} (css/get-classnames Root)
          the-list {:id 1 :items [{:id 1 :label "A"} {:id 2 :label "B"}]}]
      (dom/div #js {:className text}
        (ui-list the-list)))))

(css/upsert-css "my-css" Root)

(defcard-fulcro
  "Embedded CSS Demo"
  Root)
