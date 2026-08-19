## Integrantes del Proyecto.

**Jorge Ariel Moya de Peña**

**Jesus Manuel Bonilla Morillo**

**Lohammy Vasquez Adames**

**Adonis Mercado Hidalgo**

# Mentorly
Mentorly es un sistema de gestión de aprendizaje (LMS) diseñado para la creación de cursos y la inscripción de estudiantes. Destaca por un modelo de evaluación colaborativa (Peer Review), gamificación con controles de privacidad, e inscripciones con límite de tiempo enfocado en la obtención de certificados digitales.


## **2\. Autenticación y Roles**

### **2.1. Autenticación**

* **Onboarding / Login:** Todos los usuarios se registran e inician sesión exclusivamente mediante autenticación con cuentas de Google (Google OAuth).

### **2.2. Roles del Sistema**

* **Estudiante:** Rol por defecto al hacer login. Puede inscribirse en cursos, consumir contenido, enviar ejercicios, realizar peer reviews y ver sus estadísticas/ranking.  
* **Administrador (Admin):** Tiene acceso total. Puede crear/editar/eliminar cursos, ver todo el progreso de todos los estudiantes, auditar revisiones, ver perfiles privados y acceder a los reportes analíticos.  
* **Regla de Escalado:** Únicamente un usuario con rol de Administrador puede otorgar el rol de Administrador a otro usuario.

## **3\. Jerarquía y Estructura del Contenido**

El contenido educativo sigue una jerarquía estricta:

1. **Curso:** Contenedor principal (ej. *Android con Jetpack Compose*).

2. **Unidad:** Módulo de agrupación teórica (ej. *Unidad 1: Fundamentos*).  

3. **Tema:** Lección específica (ej. *Tema 1: Text y Button*).  
4. **Actividad (Hijo del Tema):** Puede ser un *Ejercicio* (práctico) o una *Evaluación* (Quiz).

### **3.1. Progresión del Estudiante**

* El estudiante debe marcar los temas teóricos como "Completados" para informar su progreso.  
* El avance de ejercicios y evaluaciones está sujeto a las reglas de obligatoriedad y validación por pares (Sección 4).

## **4\. Gestión de Ejercicios y Motor de Evaluación (Peer Review)**

### **4.1. Tipos de Ejercicios**

* **Opcionales:** No bloquean el progreso general del curso. Sin embargo, para que el sistema los marque como "Completados" (y otorgue la gamificación correspondiente), **el estudiante debe enviar obligatoriamente una evidencia** (texto o enlace externo). No se aprueban con solo hacer clic en un botón.  
* **Obligatorios:** El estudiante debe proporcionar obligatoriamente un enlace válido (como un repositorio de GitHub) para poder completar la tarea.  
  * **Regla de Bloqueo entre Unidades:** El estudiante debe recibir la aprobación total de un ejercicio obligatorio (vía Peer Review o Admin) para que el sistema le permita **enviar los ejercicios obligatorios de la siguiente Unidad**. (Ej. Puede leer la teoría de la Unidad 2, pero no puede enviar el ejercicio de la Unidad 2 hasta que el de la Unidad 1 esté aprobado).

### **4.2. Estrategias de Aprobación de Ejercicios**

Al crear un ejercicio obligatorio, el Admin configura su método de validación:

1. **Auto-aceptado:** Se aprueba automáticamente al enviar el enlace válido.  
2. **Validación por Pares (Peer Review):** Requiere que otros estudiantes revisen y aprueben el código.

### **4.3. Reglas del Peer Review**

* **Elegibilidad del Revisor (Regla Estricta):** Para que a un estudiante se le asigne evaluar el ejercicio de un compañero, **es requisito obligatorio que dicho estudiante ya haya enviado su propia solución** para ese mismo ejercicio (sin importar si su envío aún está pendiente de revisión o ya fue aprobado).  
* **Bloqueo Condicionado por Cuota:** El estudiante no puede avanzar enviando ejercicios de la siguiente unidad hasta que cumpla con la cuota de evaluar a N compañeros (valor N configurable por el Admin **de manera específica para cada curso**).  
* **Aprobación del Envío:** El ejercicio del estudiante evaluado cambia a estado "Aprobado" cuando recibe las N validaciones positivas de sus pares.  
* **Rúbrica y Feedback:** El revisor debe usar una rúbrica básica y dejar un comentario obligatorio justificando su decisión (aprobación/rechazo).  
* **Anonimato:** La revisión debe ser doble ciega (el revisor no sabe de quién es el código, y el evaluado no sabe quién lo revisó). **Excepción de Auditoría: El Administrador tiene visibilidad total** y puede ver en todo momento la identidad tanto del autor como del evaluador para resolver disputas.  
* **Mecanismo de Escape (Fallback):** Si un ejercicio pasa mucho tiempo sin ser evaluado por falta de pares, o hay una disputa/rechazo injustificado, el estudiante puede escalar la revisión a un Administrador.

## **5\. Ciclo de Vida de la Inscripción y Certificación**

* **Ventana de Tiempo:** Las inscripciones a los cursos tienen una duración estricta de **3 meses** a partir del día de registro (Time-Bounded Enrollment).  
* **Expiración:** Si el estudiante no completa el 100% de los requisitos (temas y ejercicios aprobados) en esos 3 meses, el estado de la inscripción cambia a "Expirado" y pierde el acceso al envío de entregas y evaluaciones.  
* **Reinicio (Borrón y Cuenta Nueva):** Para obtener el certificado tras una expiración, el estudiante debe "iniciar nuevamente". Se crea un nuevo intento de inscripción que reinicia su progreso a 0% para garantizar que domine el material actual.  
* **Certificado Digital:** Se emite automáticamente al completar el 100% del curso en estado "Completado" dentro de la ventana de 3 meses permitida.

## **6\. Privacidad y Gamificación**

### **6.1. Gamificación**

* **Incentivos:** El sistema otorga puntos e insignias por enviar temas, aprobar evaluaciones y, especialmente, por realizar evaluaciones constructivas de *Peer Review*.  
* **Ranking (Leaderboard):** Existe una tabla de clasificación por cohorte/curso para motivar a los estudiantes basada en los puntos acumulados.

### **6.2. Privacidad de Estudiantes (Opt-out)**

* **Visibilidad General:** Los estudiantes pueden ver la lista de sus compañeros de curso, pero **no pueden ver el progreso académico** de los demás.  
* **Privacidad del Leaderboard:**  
  * Por defecto, el perfil del estudiante es público y visible en el ranking para sus compañeros.  
  * **Opt-out:** El estudiante tiene un control en su perfil para cambiar su estado a "Privado".  
  * Al ser Privado, desaparece de la vista del ranking de sus compañeros y de la lista general (aunque él mismo sigue viendo su propia posición).  
  * El Administrador puede ver el ranking completo, incluyendo a los estudiantes privados (identificados con un indicador visual).

## **7\. Panel de Administración y Reportes**

El Administrador cuenta con un panel de control exclusivo dividido en herramientas operativas y de análisis:

### **7.1. Funcionalidades Operativas**

* **Gestión de Cursos (Crear/Editar):** El Admin es el único rol con capacidad para estructurar nuevos cursos. Puede definir las Unidades, Temas y establecer qué ejercicios son opcionales u obligatorios, así como su estrategia de aprobación.  
    * **Configuración del Motor de Evaluación por Curso:** Dentro de las propiedades del curso, el Admin define el valor N **(Cuota de Peer Review)** específica para ese curso, indicando cuántas revisiones de pares necesita un ejercicio para aprobarse y cuántas debe completar un estudiante como requisito.  
* **Promover Estudiante a Admin:** Un Administrador puede buscar el perfil de un usuario registrado como "Estudiante" y elevar sus privilegios al rol de "Administrador". Esta acción es la única forma en que se escalan los permisos en la plataforma.

### **7.2. Analíticas y Reportes**

* Tasas y métricas de abandono (Drop-off).  
* Tiempo promedio de finalización por Unidad / Curso.  
* Estado de cuellos de botella (ej. ejercicios atascados en la cola de Peer Review).  
* Historial de expiraciones, reinicios e intentos de inscripciones.

## **8\. Requisitos No Funcionales**

* **Accesibilidad y Diseño:** La plataforma debe contar con un diseño responsivo (Responsive Design), garantizando que la experiencia sea fluida tanto en dispositivos móviles, tablets, como en pantallas de escritorio.

## Modelo Entidad-Relación
``` mermaid
erDiagram
    USER {
        string id PK "Google UID"
        string email
        string display_name
        string role "Student, Admin"
        boolean is_leaderboard_public "Opt-out (Default true)"
        int total_points "Gamificación"
    }

    COURSE {
        string id PK
        string title
        string description
        string created_by_admin_id FK
        boolean is_published
        int required_peer_reviews "Valor N específico para este curso"
    }

    COURSE_IMAGE {
        string id PK
        string course_id FK
        string image_url
        string alt_text
        boolean is_cover "Imagen principal del curso"
        int order_index
    }

    UNIT {
        string id PK
        string course_id FK
        string title
        int order_index
    }

    THEME {
        string id PK
        string unit_id FK
        string title
        string content_text
        int order_index
    }

    ACTIVITY {
        string id PK
        string theme_id FK
        string title
        string type "Exercise, Quiz"
        boolean is_mandatory
        string approval_strategy "Auto, PeerReview, Admin"
    }

    ENROLLMENT {
        string id PK
        string student_id FK
        string course_id FK
        int attempt_number "1, 2, 3..."
        datetime started_at
        datetime expires_at "started_at + 3 meses"
        string status "Active, Expired, Completed"
        string certificate_url "Nullable"
    }

    SUBMISSION {
        string id PK
        string enrollment_id FK
        string activity_id FK
        string evidence_url "GitHub, Drive, etc."
        string status "Pending, Approved, Rejected, Escalated"
        datetime submitted_at
    }

    PEER_REVIEW {
        string id PK
        string submission_id FK
        string reviewer_student_id FK "Regla: Debe haber entregado el suyo"
        boolean is_approved
        string feedback_comment
        datetime created_at
    }

    %% Relaciones de Usuario
    USER ||--o{ COURSE : "crea (solo Admin)"
    USER ||--o{ ENROLLMENT : "se inscribe (Intento)"
    USER ||--o{ PEER_REVIEW : "evalúa como par (Ciego)"
    
    %% Relaciones de Jerarquía de Contenido
    COURSE ||--o{ COURSE_IMAGE : "tiene"
    COURSE ||--o{ UNIT : "contiene"
    UNIT ||--o{ THEME : "contiene"
    THEME ||--o{ ACTIVITY : "contiene"
    
    %% Relaciones de Flujo de Progreso
    COURSE ||--o{ ENROLLMENT : "recibe"
    ENROLLMENT ||--o{ SUBMISSION : "genera (por Intento)"
    ACTIVITY ||--o{ SUBMISSION : "recibe"
    
    %% Relaciones de Evaluación
    SUBMISSION ||--o{ PEER_REVIEW : "recibe (N requeridas)"

```

## Casos de uso
``` mermaid
flowchart LR
    %% Estilos de Nodos
    classDef actor fill:#f9f9f9,stroke:#333,stroke-width:2px,color:#000;
    classDef usecase fill:#e1f5fe,stroke:#0288d1,stroke-width:2px,color:#000,rx:20,ry:20;

    %% Actores
    Student(("🧑‍🎓 Estudiante")):::actor
    Admin(("👨‍💻 Administrador")):::actor

    %% Subsistema: Autenticación y Perfil
    subgraph Sistema_Perfil [Autenticación y Perfil]
        UC_Login(["Iniciar sesión (Google OAuth)"]):::usecase
        UC_Privacy(["Configurar Privacidad (Opt-out)"]):::usecase
        UC_ViewLeaderboard(["Ver Ranking (Leaderboard)"]):::usecase
    end

    %% Subsistema: Aprendizaje
    subgraph Sistema_Aprendizaje [Gestión Académica]
        UC_Enroll(["Inscribirse en Curso (Ciclo 3 meses)"]):::usecase
        UC_Consume(["Consumir Temas (Marcar completado)"]):::usecase
        UC_Submit(["Enviar Ejercicio (URL externa)"]):::usecase
    end

    %% Subsistema: Evaluación
    subgraph Sistema_Evaluacion [Motor de Evaluación]
        UC_Review(["Evaluar Código de Compañeros"]):::usecase
        UC_Escalate(["Escalar revisión demorada/injusta"]):::usecase
        UC_Audit(["Auditar entregas y resolver disputas"]):::usecase
    end

    %% Subsistema: Administración
    subgraph Sistema_Admin [Gestión y Configuración]
        UC_CreateCourse(["Crear/Editar Cursos y Unidades"]):::usecase
        UC_ConfigN(["Configurar Cuota Peer Review (N) por Curso"]):::usecase
        UC_Promote(["Promover Estudiante a Admin"]):::usecase
        UC_Analytics(["Ver Analíticas (Drop-off, Tiempos)"]):::usecase
    end

    %% Relaciones del Estudiante
    Student --> UC_Login
    Student --> UC_Privacy
    Student --> UC_ViewLeaderboard
    Student --> UC_Enroll
    Student --> UC_Consume
    Student --> UC_Submit
    Student --> UC_Review
    Student --> UC_Escalate

    %% Relaciones del Administrador
    Admin --> UC_Login
    Admin --> UC_Audit
    Admin --> UC_CreateCourse
    Admin --> UC_ConfigN
    Admin --> UC_Promote
    Admin --> UC_Analytics

    %% Notas de reglas de negocio
    UC_Submit -.->|Habilita| UC_Review
    UC_Review -.->|Bloquea envio de obligatorios de la siguiente unidad si no se cumple N| UC_Submit
```

### Descripción de los Flujos Principales

Gestión Académica: El estudiante inicia su ventana de 3 meses al ejecutar Inscribirse en Curso. A partir de ahí, Consumir Temas y Enviar Ejercicio son sus acciones diarias.

Ciclo de Peer Review: Como se observa, existe una dependencia (líneas punteadas). El estudiante debe haber enviado su ejercicio (Enviar Ejercicio) para poder Evaluar Código, y a su vez, esta evaluación actúa como candado para poder seguir enviando entregas de la siguiente unidad.

Privilegios de Admin: Las acciones del Administrador están completamente aisladas en la gestión del contenido, configuración de reglas (ahora por curso) y auditoría (rompiendo el doble ciego de las revisiones).
