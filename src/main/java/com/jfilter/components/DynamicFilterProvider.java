package com.jfilter.components;

import com.comparator.Comparator;
import com.jfilter.filter.DynamicFilter;
import com.jfilter.filter.DynamicFilterComponent;
import com.jfilter.filter.DynamicFilterEvent;
import com.jfilter.filter.FilterFields;
import com.jfilter.request.RequestSession;
import com.jfilter.util.FilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Dynamic filter provider
 *
 * <p>This component finds and provides Dynamic filters which annotated by {@link DynamicFilterComponent} annotation.
 * Could be used for retrieving {@link FilterFields} from {@link MethodParameter} which annotated by {@link DynamicFilter}
 */
@SuppressWarnings("CanBeFinal")
@Component
public final class DynamicFilterProvider {
    private ApplicationContext applicationContext;
    private Map<Class<? extends DynamicFilterEvent>, DynamicFilterEvent> dynamicFilterMap;

    /**
     * Creates a new instance of the {@link DynamicFilterProvider} class.
     *
     * @param applicationContext {@link ApplicationContext} bean
     */
    @Autowired
    public DynamicFilterProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.dynamicFilterMap = new HashMap<>();
        findDynamicFilters();
    }

    /**
     * Find dynamic filter beans
     *
     * <p>Attempts to find all components which annotated by {@link DynamicFilterComponent}
     * and inherited from {@link DynamicFilterEvent}
     * For example of component please see {@link DynamicSessionFilter}
     */
    private void findDynamicFilters() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(DynamicFilterComponent.class);

        beans.forEach((k, v) -> {
            if (v instanceof DynamicFilterEvent)
                dynamicFilterMap.put(((DynamicFilterEvent) v).getClass(), (DynamicFilterEvent) v);
        });
    }

    /**
     * Check if method has {@link DynamicFilter} annotation
     *
     * @param methodParameter method parameter
     * @return true if annotation is found, otherwise false
     */
    public static boolean isAccept(MethodParameter methodParameter) {
        return FilterUtil.getDeclaredAnnotations(methodParameter, DynamicFilter.class).size() > 0;
    }

    /**
     * Returns {@link FilterFields} from annotated method
     *
     * <p>Attempts to find and return dynamic filter from dynamicFilterMap
     *
     * @param methodParameter method parameter
     * @param request         service request
     * @return if found dynamic filter returns {@link FilterFields}, otherwise empty FilterFields
     */
    public FilterFields getFields(MethodParameter methodParameter, RequestSession request) {
        FilterFields filterFields = FilterFields.EMPTY_FIELDS.get();

        //Retrieve filterable fields from dynamic filters from whole class and method
        FilterUtil.getDeclaredAnnotations(methodParameter, DynamicFilter.class)
                .forEach(annotation -> filterFields.appendToMap(getFilterFields((DynamicFilter) annotation, request)));

        return filterFields;
    }

    public FilterFields getFilterFields(DynamicFilter dynamicFilter, RequestSession request) {
        if (dynamicFilter != null && dynamicFilterMap.containsKey(dynamicFilter.value())) {
            DynamicFilterEvent filter = dynamicFilterMap.get(dynamicFilter.value());

            Comparator<RequestSession, FilterFields> comparator = Comparator.of(request, FilterFields.class);
            filter.onRequest(comparator);

            //Get FilterFields from comparator or EMPTY_FIELDS if comparator not modified or conditions in compare method returned false
            return comparator.orElse(FilterFields.EMPTY_FIELDS.get());
        } else
            return FilterFields.EMPTY_FIELDS.get();
    }
}
