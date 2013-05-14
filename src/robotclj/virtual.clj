(ns robotclj.virtual
  (:require [clojure.core.reducers :as r]))




(defn create-simulator
  "Create a simulator. Clockrate is set to 1 execution per second"
  [startposition startangle drivespeed rotatespeed]
  {

   :robots  #{{:drivespeed drivespeed
               :rotatespeed rotatespeed
               :position startposition
               :angle startangle
               :proximity 0
               :lightintensity 0}}

   :parameters {:move-step 0.01
                :rotate-step 0.01
                :clockrate 1
                :active false
                }
   })

(defn calculate-rate [step speed clockrate]
  "Calculate the "
  (* clockrate (/ speed step)))

(defn norm2-distance  [[x1 y1] [x2 y2]]
  "Euclidean distance between 2 points"
  (Math/sqrt
   (+ (Math/pow (- x1 x2) 2)
      (Math/pow (- y1 y2) 2))))

(defn add-vector [v1 v2]
  "Add two vectors elements pairwise"
  (map + v1 v2))

(defn forward [position angle move-step]
  "Return a function that expects the distance to travel forward"
  (add-vector
   position
   [(* move-step (Math/cos angle)) (* move-step (Math/sin angle))]))

(defn backward [position angle]
  "Return a function that expects the distance to travel backwards"
  (forward position (+ 180 angle)))

(def direction-map
  {:forward forward :backward backward})


(defn travelled? [initrobot distance]
  (fn [robot]
    (>= (norm2-distance (:position initrobot) (:position robot)  distance))))

(def drive-cmd
  {:type ::drivestep
   :mutator (fn [robot]
              (update-in robot [:position] #(forward % (:angle robot) 0.1)))})

(def turn-cmd
  {:type ::turnstep
   :mutator (fn [robot]
              (update-in robot [:angle] #(+ % 0.1)))})


;;
;; A robot must be assigned an action-id.
;; This id will be mapped on a function (mutator) that will
;; mutate a robot (producing the next value)
;; External users can change this mutator function, providing a way to
;; give and change commands
;;


(defn start-simulator [simulator simcmd]
;  (loop [cmd  @simcmd]
 ;   (println (-> @simulator :robot))
  ;  (swap! simulator #(update-robot % (:mutator cmd)))
   ; (Thread/sleep 1000)
    ;(recur @simcmd)))
)


(defn -main [& args]
  (let [simulator (atom (create-simulator [0 0] 0  1 1))
        cmd (atom turn-cmd)]
    (start-simulator simulator cmd)))
