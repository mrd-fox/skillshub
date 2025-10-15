package com.simplon_project.skillhub.skillhub.storage.config.resolver;

import com.simplon_project.skillhub.skillhub.storage.config.UploaderId;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UploaderIdResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UploaderId.class)
                && parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        Authentication auth = (Authentication) webRequest.getUserPrincipal();
        String authName = (auth != null && auth.getName() != null && !auth.getName().isBlank())
                ? auth.getName()
                : null;

        String debugUserId = webRequest.getHeader("X-User-Id");

        return authName != null
                ? authName
                : (debugUserId != null && !debugUserId.isBlank() ? debugUserId : "anonymous");
    }
}
