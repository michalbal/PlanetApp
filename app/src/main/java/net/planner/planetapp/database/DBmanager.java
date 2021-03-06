package net.planner.planetapp.database;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import net.planner.planetapp.App;
import net.planner.planetapp.IOnPlanCalculatedListener;
import net.planner.planetapp.IOnPreferenceReceivedFromDB;
import net.planner.planetapp.IOnTaskReceivedFromDB;
import net.planner.planetapp.IOnTasksReceivedListener;
import net.planner.planetapp.UtilsKt;
import net.planner.planetapp.database.local_database.LocalDBManager;
import net.planner.planetapp.networking.GoogleCalenderCommunicator;
import net.planner.planetapp.planner.PlannerEvent;
import net.planner.planetapp.planner.PlannerObject;
import net.planner.planetapp.planner.PlannerTag;
import net.planner.planetapp.planner.PlannerTask;
import net.planner.planetapp.planner.TasksManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DBmanager {
    private static final String TAG = "DBmanager";
    FirebaseFirestore db;
    String username;

    // Notifiers on DB update
    private ArrayList<IOnTaskReceivedFromDB> tasksReceivedListeners = new ArrayList<>();
    private ArrayList<IOnPreferenceReceivedFromDB> preferencesReceivedListeners = new ArrayList<>();

    public DBmanager(String username) {
        db = FirebaseFirestore.getInstance();
        this.username = username;

        // Update app with saved data or create new register
        db.collection("users").document(username).get().addOnCompleteListener(
                new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task task) {
                        Log.d(TAG, "Completed searching for user in firestore DB");
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Found user!");
                            readPreferences();
                            readTasks();
                        } else {
                            Log.d(TAG, "Did not find user, initialising");
                            db.collection("users").document(username).set(
                                    Collections.singletonMap("username", username), SetOptions.merge());
                        }
                    }

                });

    }

    public Boolean addTaskReceivedListener(IOnTaskReceivedFromDB listener) {
        Log.d(TAG, "addTaskReceivedListener called");
        if (!tasksReceivedListeners.contains(listener)) {
            tasksReceivedListeners.add(listener);
            return true;
        }
        return false;
    }

    public Boolean removeTaskReceivedListener(IOnTaskReceivedFromDB listener) {
        Log.d(TAG, "removeTaskReceivedListener called");
        if (!tasksReceivedListeners.contains(listener)) {
            tasksReceivedListeners.remove(listener);
            return true;
        }
        return false;
    }

    public Boolean addPreferenceReceivedListener(IOnPreferenceReceivedFromDB listener) {
        Log.d(TAG, "addPreferenceReceivedListener called");
        if (!preferencesReceivedListeners.contains(listener)) {
            preferencesReceivedListeners.add(listener);
            return true;
        }
        return false;
    }

    public Boolean removePreferenceReceivedListener(IOnPreferenceReceivedFromDB listener) {
        Log.d(TAG, "addPreferenceReceivedListener called");
        if (!preferencesReceivedListeners.contains(listener)) {
            preferencesReceivedListeners.remove(listener);
            return true;
        }
        return false;
    }

    public void writeAcceptedTasks(List<PlannerTask> acceptedTasks) {
        for (PlannerTask task : acceptedTasks) {
            saveTask(task);
        }
    }

    public void saveTask(PlannerTask task) {
            TaskDB taskDB = new TaskDB(task.getMoodleId(), task.getTitle(), task.getCourseId(),
                    task.getDescription(), task.getLocation(),
                    task.isExclusiveForItsTimeSlot(), task.getReminder(),
                    task.getTagName(), task.getDeadline(), task.getPriority(),
                    task.getMaxSessionTimeInMinutes(),
                    task.getMaxDivisionsNumber(), task.getDurationInMinutes());


            db.collection("users").document(username).collection("tasks").document(
                    task.getMoodleId()).set(taskDB, SetOptions.merge());
    }

    public void deleteTask(PlannerTask plannerTask) {
        this.deleteAllSubtasks(plannerTask.getMoodleId());

        db.collection("users").document(username).collection("tasks").document(
                plannerTask.getMoodleId()).delete();
    }

    public void writeNewSubtasks(List<PlannerEvent> acceptedEvents) {
        for (PlannerEvent subtask : acceptedEvents) {
            SubtaskDB subtaskDB = new SubtaskDB(Long.toString(subtask.getEventId()),
                                                subtask.getStartTime(), subtask.getEndTime(),
                                                subtask.isAllDay());
            String moodleId = subtask.getParentTaskId();

            db.collection("users").document(username).collection("tasks").document(moodleId)
                    .collection("subtasks").document(subtaskDB.getEventIdGC()).set(subtaskDB,
                                                                                   SetOptions
                                                                                           .merge());

        }
    }

    public void readTasks() {
        db.collection("users").document(username).collection("tasks").get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                TaskDB taskDB = document.toObject(TaskDB.class);
                                PlannerTask plannerTask = new PlannerTask(taskDB);
                                TasksManager.getInstance().addTaskFromDB(plannerTask);
                                for(IOnTaskReceivedFromDB listener : tasksReceivedListeners) {
                                    listener.onTaskReceived(plannerTask);
                                }
                            }
                        }
                    }
                });
    }

    public void deleteAllSubtasks(String courseId) {
        db.collection("users").document(username).collection("tasks").document(courseId).collection(
                "subtasks").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        SubtaskDB subtask = document.toObject(SubtaskDB.class);
                        String googleCalendarId = subtask.getEventIdGC();
                        deleteSubtask(courseId, googleCalendarId);

                    }
                }
            }
        });
    }

    public void deleteSubtask(String courseId, String subtaskId) {
        DocumentReference docRef = db.collection("users").document(username).collection("tasks")
                .document(courseId).collection("subtasks").document(subtaskId);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override public void onSuccess(DocumentSnapshot documentSnapshot) {
                SubtaskDB subtaskDB = documentSnapshot.toObject(SubtaskDB.class);
                if (subtaskDB != null) {
                    GoogleCalenderCommunicator.INSTANCE.deleteEvent(App.context, subtaskDB.getEventIdGC());
                }
            }
        });

        docRef.delete();
    }

    public void addMoodleCourseName(String courseId, String courseName) {
        HashMap<String, String> course = new HashMap<>();
        course.put("courseId", courseId);
        course.put("courseName", courseName);

        db.collection("users").document(username).collection("courses")
                .document(courseId).set(course, SetOptions.merge());
    }

    public void addMoodleCoursePreference(String courseId, String preferenceTagId) {
        HashMap<String, String> course = new HashMap<>();
        course.put("courseId", courseId);
        course.put("preferenceTagId", preferenceTagId);

        db.collection("users").document(username).collection("courses")
                .document(courseId).set(course, SetOptions.merge());
    }

    public void addUserMoodleCourses(HashMap<String, String> moodleCourses) {
        for (HashMap.Entry<String, String> parsedCourseName : moodleCourses.entrySet()) {
            LocalDBManager.INSTANCE.insertOrUpdateCourse(parsedCourseName.getKey(), parsedCourseName.getValue(), PlannerObject.GENERAL_TAG);
            addMoodleCourseName(parsedCourseName.getKey(), parsedCourseName.getValue());
        }
    }

    private HashMap<String, ArrayList<String>> flattenKey(
            HashMap<Pair<String, String>, ArrayList<String>> pairKeyMap) {
        HashMap<String, ArrayList<String>> stringKeyMap = new HashMap<>();
        for (HashMap.Entry<Pair<String, String>, ArrayList<String>> entry : pairKeyMap.entrySet()) {
            String timeInterval = entry.getKey().first + "-" + entry.getKey().second;
            stringKeyMap.put(timeInterval, entry.getValue());
        }
        return stringKeyMap;
    }

    public void addPreference(PlannerTag tag) {
        PreferenceDB preference = new PreferenceDB(tag.getTagName(), tag.getPriority(),
                                                   flattenKey(tag.getPreferredTIsettings()),
                                                   flattenKey(tag.getForbiddenTIsettings()));

        db.collection("users").document(username).collection("preferences")
                .document(preference.getTagName()).set(preference, SetOptions.merge());
    }

    public void readPreferences() {
        db.collection("users").document(username).collection("preferences")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PreferenceDB preference = document.toObject(PreferenceDB.class);
                                PlannerTag tag = new PlannerTag(preference.getTagName(),
                                                                preference.getPriority(),
                                                                preference.getForbiddenTIsettings(),
                                                                preference
                                                                        .getPreferredTIsettings());

                                TasksManager.getInstance().addPreferenceTag(tag, false);
                                for(IOnPreferenceReceivedFromDB listener : preferencesReceivedListeners) {
                                    listener.onPreferenceReceived(preference);
                                }
                            }
                        }
                    }
                });

    }

    public void readUserMoodleCourses() {
        db.collection("users").document(username).collection("courses")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                CourseDB courseDB = document.toObject(CourseDB.class);

                                TasksManager.getInstance().addMoodleCourse(courseDB.getCourseId(),
                                                                           courseDB.getCourseName(),
                                                                           false);
                                TasksManager.getInstance().addCoursePreference(
                                        courseDB.getCourseId(), courseDB.getPreferenceTagId(),
                                        false);
                            }
                        }

                        readUnwantedCourses();
                    }
                });

    }

    public void addUnwantedCourse(String courseId) {
        db.collection("users").document(username).update("unwantedCourses",
                                                         FieldValue.arrayUnion(courseId));
    }

    public void removeUnwantedCourse(String courseId) {
        db.collection("users").document(username).update("unwantedCourses",
                                                         FieldValue.arrayRemove(courseId));
    }

    public void addUnwantedTask(String moodleTaskId) {
        db.collection("users").document(username).update("unwantedTasks",
                                                         FieldValue.arrayUnion(moodleTaskId));
    }

    public void removeUnwantedTask(String moodleTaskId) {
        db.collection("users").document(username).update("unwantedTasks",
                                                         FieldValue.arrayRemove(moodleTaskId));
    }

    public void readUnwantedCourses() {
        DocumentReference docRef = db.collection("users").document(username);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    UserDB userDB = Objects.requireNonNull(task.getResult()).toObject(UserDB.class);
                    if (userDB != null) {
                        TasksManager.getInstance().setUnwantedCourseIds(userDB.getUnwantedCourses());
                    }
                }
                readUnwantedTasks();
            }
        });
    }

    public void readUnwantedTasks() {
        DocumentReference docRef = db.collection("users").document(username);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    UserDB userDB = Objects.requireNonNull(task.getResult()).toObject(UserDB.class);
                    if (userDB != null) {
                        TasksManager.getInstance().setUnwantedTaskIds(userDB.getUnwantedTasks());
                    }
                }

//                long currentTime = System.currentTimeMillis();
                long currentTime = UtilsKt.getTodayTimeMillis();
                LinkedList<PlannerTask> plannerTasks = TasksManager.getInstance().parseMoodleTasks(
                        currentTime);
            }
        });
    }

}
