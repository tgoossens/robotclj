(ns robotclj.maze)





(def traversables #{::line})

(def traversable? [edge]
  (traversables edge))


(def maze {})


(defn update-tile-at
  "Update the tile at the given position. Returns a new maze"
  [maze position tile]
  (assoc-in maze [:tiles position] tile))

(defn tile-at
  "Returns the tile at the given if it exists.
   If not, nil is returned"
  [maze position]
  (-> maze :tiles position))





;; TILES
(defn tile [edges]
  {:edges edges})

(defn rotate-edges-cw
  "Rotate clockwise"
  [edges]
  (let [n (:north edges)
        e (:east edges)
        s (:south edges)
        w (:west edges)]
    (assoc edges :north w :east n :south e :west s)))

(defn rotate-tile [tile}]
  (update-in tile [:edges] rotate-edges-cw))


;; Corner
(def corner-north {:edges {:north ::wall
                           :west ::wall}})
(def corner-east (rotate-tile corner-north))
(def corner-south (rotate-tile corner-east))
(def corner-west (rorate-tile corner-south))

;; T-tile
(def t-north {:edges {:north ::wall}}
(def t-east (rotate-tile t-north))
(def t-south (rotate-tile t-east))
(def t-west (rorate-tile t-south))

;; Dead-end
(def deadend-north {:edges {:north ::wall
                            :east :wall
                            :west ::wall}})
(def deadend-east (rotate-tile deadend-north))
(def deadend-south (rotate-tile deadend-east))
(def deadend-west (rorate-tile deadend-south))

;; Cross
(def cross {:edges {}});


;; Straight
(def straight-north {:edges {:east ::wall
                             :west ::wall}})
(def straight-east (rotate-tile straight-north))
(def straight-south (rotate-tile straight-east))
(def straight-west (rorate-tile straight-south))



(comment
  "Tile example"
  {:barcode  {:code 25 :orienation :north}
   :object true
   :edges {:north ::wall
           :east ::wall
           :south ::unknown
           :west ::line}}
  )
