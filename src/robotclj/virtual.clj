(ns robotclj.virtual
	(:use [overtone.at-at]))




(defn create-simulator 
  "Create a simulator. Clockrate is set to 1 execution per second"
  [startposition startangle travelspeed rotatespeed]
  {
   
   :command {:type :nop}

   :robot  {:travelspeed travelspeed
            :rotatespeed rotatespeed
            :position startposition
            :angle startangle
            :proximity 0
            :lightintensity 0
            }
   
   :parameters {:move-step 0.01
                :rotate-step 0.01
                :clockrate 1 
               }
   }
  )

(defn calculate-rate [step speed clockrate]
	"Calculate the "
	(* clockrate (/ speed step)))



(defn add-vector [v1 v2]
	"Add two vectors elements pairwise"
	(map + v1 v2))

(defn forward [position angle]
	"Return a function that expects the distance to travel forward"
	(fn [move-step]
		(add-vector 
     position
     [(* move-step (Math/cos angle)) (* move-step (Math/sin angle))])))

(defn backward [position angle]
	"Return a function that expects the distance to travel backwards"
 (forward position (+ 180 angle)))

(def direction-map
 {:forward forward :backward backward})



(defn drive-step [simulator drive-fn]
	"Make robot dirve a way given by move-fn.
	drive-fn must accept an position and angle and return the new position"
 (let [params (:parameters simulator)
 	     robot (:robot simulator)
 	     angle (:angle robot)
 	     position (:position robot)
 	     move-step (:move-step params)]
 (assoc-in simulator [:robot :position]
  ((drive-fn position angle) move-step))))


(defn rotate-step [simulator rotate-fn]
	"Make robot rotate a way given by rotate-fn.
	rotate-fn must accept an angle and return the new angle"
 (let [params (:parameters simulator)
 	     robot (:robot simulator)
 	     angle (:angle robot)
 	     angle-step (:angle-step params)]
  (assoc-in simulator [:robot :angle]
    ((rotate-fn angle) angle-step))))

 


;command must have stopcondition? (fn [simulator] true/false)

;does this need to be complected with command? How to decomplect this?

(defn drive-command [drive-fn stop-fn]
	"Construct a drive command. 
	Must provide a drive-fn which accepts a position and angle and returns a position.
	Must provide a stop-fn which accepts a simulator and returns true or false. Stop-fn has the role of stopcondition

	"
	{:type ::drive
	 :drive-fn drive-fn
	 :stop-fn stop-fn})


(defn rotate-command [rotate-fn stop-fn]
	"Construct a rotate command. 
	Must provide a rotate-fn which accepts angle and returns an angle.
	Must provide a stop-fn which accepts a simulator and returns true or false. 
	Stop-fn has the role of stopcondition
	"
	{:type ::rotate
	 :rotate-fn rotate-fn
	 :stop-fn stop-fn})

(defn nop-command []
	"Construct a nop command. This command does literally nothing.
	Its stop-fn will always return false.
	"
	{:type ::nop
	 :stop-fn (fn [simulator] false)})

(defmulti command-map "Maps command onto a function that accepts a simulator.
	This function will produce the next value of the simulator." :type)
(defmethod command-map ::nop [command] (fn [simulator] simulator))	
(defmethod command-map ::drive [command] (fn [simulator] (drive-step simulator (:drive-fn command))))
(defmethod command-map ::rotate [command] (fn [simulator] (rotate-step simulator (:rotate-fn command))))
(defmethod command-map ::travel [command] (fn [simulator] (drive-step simulator (:drive-fn command))))


(defn run-command [simulator command rate]
	"Expects a simulator atom"
	(loop []
		(when-not ((:stopcondition command) command)
		  	(Thread/sleep (/ 1000 rate))
		  	(swap! simulator (command-map command))
		    (recur))))

