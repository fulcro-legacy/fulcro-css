(ns om-css.cards-ui
  (:require-macros
    [devcards.core :refer [defcard-om-next]])
  (:require [om.dom :as dom]
            [om-css.css :as css]
            [om.next :as om :refer [defui]]))

(defui ListItem
  static css/CSS
  (local-rules [this] [[:.item {:font-weight "bold"}]])
  (include-children [this] [])
  Object
  (render [this]
    (let [{:keys [label]} (om/props this)
          {:keys [item]} (css/get-classnames ListItem)]
      (dom/li #js {:className item} label))))

(def ui-list-item (om/factory ListItem {:keyfn :id}))

(defui ListComponent
  static css/CSS
  (local-rules [this] [[:.items-wrapper {:background-color "blue"}]])
  (include-children [this] [ListItem])
  Object
  (render [this]
    (let [{:keys [id items]} (om/props this)
          {:keys [items-wrapper]} (css/get-classnames ListComponent)]
      (dom/div #js {:className items-wrapper}
        (dom/h2 nil (str "List " id))
        (dom/ul nil (map ui-list-item items))))))

(def ui-list (om/factory ListComponent {:keyfn :id}))

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

(defn reader [{:keys [state query]} k p]
  {:value nil})

(def parser (om/parser {:read reader}))

(defcard-om-next
  "Embedded CSS Demo"
  Root
  (om/reconciler {:state {} :parser parser}))
