# Stage 1: Build WAR bằng Maven & JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy mã nguồn và đóng gói WAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime với Apache Tomcat 9 & JRE 17
# Lưu ý: Dự án dùng Java 17 + javax.* (Servlet 4.0, JPA 2.2) 
# nên bắt buộc dùng Tomcat 9 (Tomcat 10+ chuyển sang jakarta.* sẽ gây lỗi)
FROM tomcat:9.0-jdk17-temurin-jammy

WORKDIR /usr/local/tomcat

# Dọn dẹp webapps mặc định của Tomcat
RUN rm -rf webapps/*

# Đặt ứng dụng làm ứng dụng gốc (ROOT.war) 
# để truy cập trực tiếp từ https://your-app.onrender.com/
COPY --from=builder /build/target/jpa.war webapps/ROOT.war

# Render cấp port động qua biến môi trường $PORT (mặc định 8080 hoặc 10000)
ENV PORT=10000
EXPOSE 10000

# Cập nhật cổng trong server.xml theo $PORT của Render và khởi chạy Tomcat
CMD ["sh", "-c", "sed -i 's/port=\"8080\"/port=\"'\"${PORT:-10000}\"'\"/g; s/<Server port=\"8005\"/<Server port=\"-1\"/g' conf/server.xml && catalina.sh run"]
