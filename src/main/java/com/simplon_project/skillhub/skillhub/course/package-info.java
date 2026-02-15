@org.springframework.modulith.ApplicationModule(
        displayName = "Course",
        allowedDependencies = {"common::messaging", "common", "config :: helper", "user :: api"}
)

package com.simplon_project.skillhub.skillhub.course;