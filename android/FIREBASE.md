# Configurar Firebase para Catanna, paso a paso

Se hace una sola vez y lleva unos 20 minutos. Necesitas una cuenta de Google y, por el paso 2, una tarjeta.
Los nombres de los botones son los de la consola en español; si la tienes en inglés, van entre paréntesis.

Al final tendrás:

- un proyecto de Firebase con login por correo, base de datos (Firestore) y almacén de fotos (Storage);
- el archivo `google-services.json` dentro de `android/app/`;
- tu cuenta marcada como maestra.

---

## 1. Crear el proyecto

1. Entra en <https://console.firebase.google.com> con tu cuenta de Google.
2. **Crear un proyecto** (*Create a project*).
3. Nombre: `catanna` (o el que quieras). Apunta el **ID del proyecto** que aparece debajo del nombre, p. ej. `catanna-1a2b3`; lo usarás en el paso 7.
4. Te ofrece **Gemini en Firebase** y **Google Analytics**: desactiva los dos. La app no los usa.
5. **Crear proyecto** y espera a que termine.

## 2. Pasar al plan Blaze (obligatorio para las fotos)

Desde febrero de 2026, Google exige el plan **Blaze** (pago por uso) para usar Storage, que es donde van las fotos de los productos. Blaze incluye las mismas cuotas gratis que el plan gratuito, así que con una app privada **la factura debería ser 0 €**: hablamos de unos cientos de fotos de ~200 KB.

1. Abajo a la izquierda, junto a «Spark», pulsa **Actualizar** (*Upgrade*) → **Blaze**.
2. Crea o elige una cuenta de facturación de Google Cloud y añade la tarjeta.
3. Cuando te ofrezca **fijar un presupuesto** (*Set a budget*), pon **1 €** (o 5 €). Solo envía un correo de aviso si se supera; no corta nada, pero te enteras.

## 3. Registrar las dos apps Android

La de cliente y la maestra son dos apps distintas para Firebase, pero comparten proyecto (y por tanto datos).

1. En la página principal del proyecto, pulsa el icono de **Android** (o ⚙ **Configuración del proyecto** → *General* → **Agregar app** → Android).
2. **Nombre del paquete de Android**: `com.catanna.app` — exactamente así.
   - Apodo: `Catanna`.
   - **Certificado SHA-1**: déjalo vacío. Solo hace falta para el login con Google, y aquí usamos correo.
3. **Registrar app**. En el paso de descargar el archivo, de momento pulsa **Siguiente** sin descargar, y luego **Siguiente** / **Ir a la consola** en el resto (el código ya está preparado; ignora las instrucciones de Gradle que te enseña).
4. Repite con la segunda app:
   - paquete: `com.catanna.app.maestra`
   - apodo: `Catanna Maestra`

## 4. Descargar `google-services.json`

Ahora que existen las dos apps, el archivo las incluye a ambas.

1. ⚙ **Configuración del proyecto** → pestaña **General** → baja hasta **Tus apps**.
2. Elige cualquiera de las dos y pulsa **google-services.json** para descargarlo.
3. Cópialo a `android/app/google-services.json` (al lado de `build.gradle.kts`). El nombre tiene que ser exactamente ese.
4. Comprueba que dentro aparecen los dos paquetes: ábrelo con un editor y busca `"package_name"`; deben salir `com.catanna.app` y `com.catanna.app.maestra`. Si solo sale uno, lo descargaste antes de registrar la segunda app: vuelve a descargarlo.

> Este archivo no es secreto (identifica el proyecto, no da acceso; lo que protege los datos son las reglas del paso 7). Se puede subir al repositorio.

## 5. Activar el login por correo

1. Menú izquierdo → **Compilación** (*Build*) → **Authentication** → **Comenzar** (*Get started*).
2. Pestaña **Método de acceso** (*Sign-in method*) → **Correo electrónico/contraseña** (*Email/Password*).
3. Activa el **primer** interruptor (*Email/Password*). El segundo (*Vínculo de correo electrónico*) déjalo apagado. **Guardar**.
4. Opcional pero recomendable: pestaña **Plantillas** (*Templates*) → icono del lápiz/idioma → **Idioma de las plantillas: español**. Así el correo de «He olvidado la contraseña» llega en español.

## 6. Crear la base de datos y el almacén de fotos

### Firestore

1. **Compilación** → **Firestore Database** → **Crear base de datos**.
2. Edición **Standard**, ID de la base de datos `(default)` — no lo cambies, la app usa esa.
3. **Ubicación**: `europe-southwest1 (Madrid)`. No se puede cambiar después.
4. **Iniciar en modo de producción** (*production mode*). Todo queda cerrado hasta que pongas las reglas del paso 7.
5. **Crear**.

### Storage

1. **Compilación** → **Storage** → **Comenzar**.
2. **Ubicación del bucket**: elige una de las marcadas como **sin coste** (*No cost*), p. ej. `US-CENTRAL1`. Solo esas tienen la cuota gratuita de 5 GB; una europea costaría céntimos al mes, así que tampoco pasa nada si prefieres Europa.
3. **Iniciar en modo de producción** → **Crear**.

## 7. Poner las reglas de seguridad

Las reglas son las que hacen la app privada: sin cuenta no se ve nada, cada persona solo puede escribir su propia valoración y solo las cuentas maestras tocan el catálogo y las fotos. Están en `android/firebase/`.

### Opción A: copiar y pegar (más fácil)

1. **Firestore Database** → pestaña **Reglas** (*Rules*). Borra todo lo que hay, pega el contenido completo de `android/firebase/firestore.rules` y pulsa **Publicar**.
2. **Storage** → pestaña **Reglas**. Borra todo, pega `android/firebase/storage.rules` y **Publicar**.
   - Te avisará de que las reglas de Storage consultan Firestore (para saber si eres maestra) y te pedirá **conceder permiso** (*Grant access*). Acéptalo; si no, la maestra no podrá subir fotos.

### Opción B: con la línea de comandos

Necesitas Node.js instalado.

```
npm install -g firebase-tools
firebase login
cd android/firebase
firebase deploy --only firestore:rules,storage --project catanna-1a2b3
```

(cambia `catanna-1a2b3` por tu ID del paso 1).

## 8. Compilar e instalar

1. Instala [Android Studio](https://developer.android.com/studio) y abre la carpeta **`android/`** (no la raíz del repositorio).
2. Espera a que termine de sincronizar Gradle (la primera vez descarga bastante).
3. Menú **Build → Select Build Variant…** y elige `clienteDebug` o `maestraDebug`.
4. Conecta el móvil con la depuración USB activada (o usa un emulador) y pulsa ▶.

Si al abrir la app sale «Falta configurar Firebase», es que `google-services.json` no está en `android/app/` o tiene otro nombre: corrígelo y vuelve a compilar.

## 9. Hacerte maestra

1. Instala y abre **Catanna Maestra**, pulsa **Crear una cuenta** y regístrate con tu nombre, correo y contraseña.
2. Saldrá «Esta cuenta no es maestra» con un código largo (tu **id de usuario**). Pulsa **Copiar el id**.
   - También lo encuentras en la consola: **Authentication → Usuarios**, columna *UID de usuario*.
3. En la consola: **Firestore Database** → pestaña **Datos** → **+ Iniciar colección** (*Start collection*).
   - ID de la colección: `admins`
   - **Siguiente**.
   - ID del documento: pega tu id (no pulses «ID automático»).
   - Campo: `nombre`, tipo *string*, valor: tu nombre (el contenido da igual; la consola pide al menos un campo).
   - **Guardar**.
4. Vuelve a la app: pasa sola a la maestra, sin reiniciar.

Para dar permiso a otra persona, repite el paso 3 con su id (en la colección `admins` ya creada → **+ Agregar documento**). Para quitárselo, borra su documento.

## 10. Cargar la carta y probar

1. En la maestra → **Catálogo** → **Cargar la carta de ejemplo**. Sube 16 productos (5 con foto). Si no quieres los de ejemplo, da de alta los tuyos desde **Nuevo**.
2. Instala la app de cliente (`clienteDebug`), entra con la misma cuenta u otra, y valora algo. La nota y las etiquetas cambian al momento en las dos apps.

---

## Si algo falla

| Síntoma | Causa probable |
|---|---|
| «Falta configurar Firebase» | `google-services.json` no está en `android/app/`. |
| Gradle: *No matching client found for package name 'com.catanna.app.maestra'* | El json se descargó con una sola app registrada. Descárgalo otra vez (paso 4). |
| «No se ha podido entrar (ERROR_OPERATION_NOT_ALLOWED)» | Falta activar Correo/contraseña (paso 5). |
| La carta sale vacía y no carga nunca | Faltan las reglas de Firestore (paso 7) o no existe la base de datos `(default)`. |
| La maestra dice «No se ha podido guardar» al publicar con foto | Faltan las reglas de Storage, no aceptaste el permiso de Storage→Firestore, o el proyecto no está en Blaze. |
| Sigue saliendo «Esta cuenta no es maestra» | El id del documento en `admins` no es exactamente tu UID (sin espacios), o la colección no se llama `admins`. |

Para ver el error exacto: en Android Studio, pestaña **Logcat**, filtra por `Repo` o `Maestra`.
