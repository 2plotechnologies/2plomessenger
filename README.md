# 2Plo Messenger

Aplicación Android de mensajería social desarrollada en Java con Firebase.

## Descripción
2Plo Messenger permite:
- Registro e inicio de sesión de usuarios.
- Chats privados, grupos y canales.
- Publicación de posts y comentarios.
- Envío de multimedia (imágenes, audio, video y archivos).
- Videollamadas con integración de Jitsi.
- Notificaciones push con Firebase Cloud Messaging.
- Funciones de privacidad y configuración de cuenta.

## Stack técnico
- **Lenguaje:** Java
- **Plataforma:** Android (minSdk 24, targetSdk 36)
- **Servicios:** Firebase (Auth, Realtime Database, Storage, Firestore, Messaging, Functions, Vertex AI)
- **Build system:** Gradle (Android Gradle Plugin 8.13.2)

## Estructura del proyecto
- `app/src/main/java/com/twoploapps/a2plomessenger/`: Activities, Fragments, adapters, modelos y controladores.
- `app/src/main/res/`: layouts, recursos visuales, strings y temas.
- `app/src/main/AndroidManifest.xml`: permisos, actividades y servicios.
- `app/build.gradle`: configuración del módulo y dependencias.

## Requisitos
- Android Studio actualizado.
- JDK 8+.
- Archivo de configuración de Firebase (`app/google-services.json`).

## Ejecución rápida
1. Abrir el proyecto en Android Studio.
2. Sincronizar Gradle.
3. Verificar configuración de Firebase.
4. Ejecutar en emulador o dispositivo Android.
