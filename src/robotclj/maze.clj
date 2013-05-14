(ns robotclj.maze)



(def tile {:barcode  {:code 25 :orienation :north}
           :edges {:north ::wall
                   :east ::wall
                   :south ::unknown
                   :west ::line}})

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
