(ns robotclj.virtual)




(defn create-simulator 
  "Create a simulator. Clockrate is set to 1 execution per second"
  [startposition startangle drivespeed rotatespeed]
  {
   
   :command {:type :nop}
    
   :commandqueue []

   :robot  {:drivespeed drivespeed
            :rotatespeed rotatespeed
            :position startposition
            :angle startangle
            :proximity 0
            :lightintensity 0
            }
   
   :parameters {:move-step 0.01
                :rotate-step 0.01
                :clockrate 1 
                :active false
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


;RATE defined in a command? Why would you do that? What does this even mean?
;Should rate return a function that expects a simulator and then returns the rate?
; (fn [simulator] (/ (-> simulator :robot :drivespeed ) (-> simulator :parameters :move-step))

(defn drive-command [drive-fn stop-fn]
	"Construct a drive command. 
	Must provide a drive-fn which accepts a position and angle and returns a position.
	Must provide a stop-fn which accepts a simulator and returns true or false. Stop-fn has the role of stopcondition

	"
	{:type ::drive
	 :drive-fn drive-fn
	 :stop-fn stop-fn
	 :rate (fn [simulator] 
	 	      (/ (-> simulator :robot :drivespeed ) (-> simulator :parameters :move-step))) })


(defn rotate-command [rotate-fn stop-fn]
	"Construct a rotate command. 
	Must provide a rotate-fn which accepts angle and returns an angle.
	Must provide a stop-fn which accepts a simulator and returns true or false. 
	Stop-fn has the role of stopcondition
	"
	{:type ::rotate
	 :rotate-fn rotate-fn
	 :stop-fn stop-fn
	 :rate (fn [simulator] 
	 	      (/ (-> simulator :robot :rotatespeed ) (-> simulator :parameters :rotate-step)))})

(defn nop-command []
	"Construct a nop command. This command does literally nothing.
	Its stop-fn will always return false.
	"
	{:type ::nop
	 :stop-fn (fn [simulator] false)
	 :rate (fn [simulator] 10)})

(defmulti command-map "Maps command onto a function that accepts a simulator.
	This function will produce the next value of the simulator." :type)
(defmethod command-map ::nop [command] (fn [simulator] simulator))	
(defmethod command-map ::drive [command] (fn [simulator] (drive-step simulator (:drive-fn command))))
(defmethod command-map ::rotate [command] (fn [simulator] (rotate-step simulator (:rotate-fn command))))
(defmethod command-map ::travel [command] (fn [simulator] (drive-step simulator (:drive-fn command))))


; replace by command queue

;use agent to model this: fn swaps the currently running command? 


;add commands to queue. which will be executed in order. Unless some of the commands is a stop command.
;Then the whole queue gets reset and all action stops



(defn push-command [simulator-atom command]
	(swap! simulator-atom 
		(fn [simulator]
			(let [queue (:commandqueue simulator)]
				(assoc simulator :commandqueue (conj queue command))))))


(defn stop? [command]
	(= ::stop (:type command)))

(defn ready? [{:keys [enabled]}]
	(boolean (not enabled)))

(defn stop-task [{:keys [enabled] :as task}]
	(assoc task :enabled false))


;The currently running thread is kept in the recur binding. so that checking can continue.
;dereferences simulator before every new loop

   
		
(defn run-task [simulator command]
	"Expects a simulator atom"
	(loop []
		(when-not (or ((:stop-fn @command) @simulator) 
			            (not (:enabled @command)))
		  	(Thread/sleep 1000)
		  	(swap! simulator (command-map @command))
		
		    (recur))))


;What a monster

(defn start-simulator [simulator-atom]
"Expects a simulator atom"
  (swap! simulator-atom assoc-in [:parameters :active] true)

	(loop [running (atom nil) simulator @simulator-atom]
		(Thread/sleep 1000)
		(if (empty? (:commandqueue simulator))
  	 (recur running @simulator-atom)
     (if-not (some stop? (:commandqueue simulator))
      (if (ready? @running)
       (let [command (first (:commandqueue simulator))
      			 task (atom (assoc command :enabled true))]
      	 (swap! simulator-atom #(assoc % :commandqueue (rest (:commandqueue %))))
      	 (println @task)
      	 (.start (Thread. (fn [] (run-task simulator-atom task))))
      	 (recur task @simulator-atom))  

      (recur running @simulator-atom))
     (do 
      (when-not (nil? @running)
       (swap! running stop-task))
       (swap! simulator-atom assoc :commandqueue [])
      (recur (atom nil) @simulator-atom))))))


(defn -main [& args]
	(start-simulator (atom (create-simulator [0 0] 0  1 1))))