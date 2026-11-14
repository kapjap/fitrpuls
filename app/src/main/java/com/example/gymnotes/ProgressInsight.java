package com.example.gymnotes;

import java.util.*;
/** Descriptive evidence, never an automatic load prescription. Names are legacy identities. */
public final class ProgressInsight {
    public static String explain(List<CompletedExerciseEntity> newestFirst) {
        Map<String, LinkedHashMap<Integer, Double>> records=new LinkedHashMap<>();
        Map<String,String> titles=new HashMap<>();
        for(CompletedExerciseEntity e:newestFirst) {
            if(e.warmup || e.getName()==null || e.getSets()<=0 || e.getReps()<=0) continue;
            String key=e.getName().trim().toLowerCase(Locale.ROOT); if(key.isEmpty())continue;
            titles.put(key,e.getName().trim());
            LinkedHashMap<Integer,Double> workouts=records.computeIfAbsent(key,k->new LinkedHashMap<>());
            workouts.put(e.getCompletedWorkoutId(),Math.max(workouts.getOrDefault(e.getCompletedWorkoutId(),0d),e.getWeight()));
        }
        for(Map.Entry<String,LinkedHashMap<Integer,Double>> record:records.entrySet()) {
            if(record.getValue().size()<2)continue;
            Iterator<Double> weights=record.getValue().values().iterator();double current=weights.next(),previous=weights.next();
            return String.format(Locale.getDefault(),"%s: максимальный рабочий вес %.1f → %.1f кг в двух последних тренировках с этим названием. Разминка исключена. Повторения, техника и оборудование могли отличаться; это наблюдение, не рекомендация увеличить нагрузку.",titles.get(record.getKey()),previous,current);
        }
        return "Для сравнения нужны две сохранённые тренировки с одним упражнением. Сравниваем упражнение только с ним самим, без рейтинга разных движений. Старые записи 2.0 могут включать плановые подходы.";
    }
    private ProgressInsight(){}
}
