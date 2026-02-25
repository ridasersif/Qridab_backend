# المرحلة 1: بناء الكود
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1. كوبي الـ pom.xml باش يتيليشارجي الـ dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. دابا كوبي الكود (src) - هادي هي المرحلة المهمة
COPY src ./src

# 3. ابني الـ JAR
RUN mvn clean package -DskipTests

# المرحلة 2: تشغيل الكود (صورة خفيفة)
FROM eclipse-temurin:21-jdk
WORKDIR /app
# كوبي غير الـ JAR اللي تكرى في المرحلة الأولى
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]