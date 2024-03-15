package fr.smourad.chaos.event;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Component("discordEventLoader")
public class DiscordEventLoader implements BeanPostProcessor {

    private final EventDispatcher eventDispatcher;

    public DiscordEventLoader(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public Object postProcessBeforeInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if (bean instanceof DiscordEventListener) {
            ReflectionUtils.doWithMethods(bean.getClass(), method -> registerEvents(bean, method));
        }

        return bean;
    }

    @SuppressWarnings("unchecked")
    protected void registerEvents(Object bean, Method method) {
        if (method.isAnnotationPresent(DiscordEventHandler.class)) {
            Parameter parameter = method.getParameters()[0];
            Class<?> type = parameter.getType();

            if (Event.class.isAssignableFrom(type)) {
                Class<? extends Event> eventType = (Class<? extends Event>) type;
                registerEvent(method, bean, eventType);
            }
        }
    }

    protected void registerEvent(Method method, Object bean, Class<? extends Event> eventType) {
        eventDispatcher
                .on(eventType)
                .flatMap(event -> {
                    Object response = ReflectionUtils.invokeMethod(method, bean, event);
                    if (response instanceof Publisher<?> publisher) {
                        return publisher;
                    }

                    return Mono.empty();
                })
                .doOnError(error -> {
                    error.printStackTrace();
                    registerEvent(method, bean, eventType);
                })
                .log()
                .subscribe();
    }


}
