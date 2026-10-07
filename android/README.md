# Catanna para Android

Implementación nativa (Kotlin + Jetpack Compose) del diseño `project/Catanna Social.dc.html`.
Un mismo proyecto genera dos apps que comparten los datos en Firebase:

| Variante  | Para quién | Qué hace |
|-----------|------------|----------|
| `cliente` → **Catanna** | cualquiera con cuenta | Descubrir, Carta, Probados, ficha, etiqueta y valorar |
| `maestra` → **Catanna Maestra** | cuentas marcadas como maestras | Nuevo producto (con foto), Catálogo y Etiquetas |

Si publicas un producto en la maestra, aparece al momento en la de cliente. Si alguien valora en la de cliente, la nota, la potencia y las etiquetas se recalculan al momento en las dos.

## Puesta en marcha

**Sigue [`FIREBASE.md`](FIREBASE.md)**: crear el proyecto de Firebase, registrar las dos apps, activar login, base de datos y fotos, poner las reglas, compilar y hacerte maestra. En resumen:

1. Proyecto de Firebase en plan Blaze (Storage lo exige; con este uso sale a 0 €).
2. Dos apps Android: `com.catanna.app` y `com.catanna.app.maestra`. Su `google-services.json` va en `android/app/`.
3. Authentication por correo/contraseña, Firestore `(default)` y Storage, con las reglas de `android/firebase/`.
4. Abrir `android/` en Android Studio y ejecutar `clienteDebug` o `maestraDebug`.
5. Crear tu cuenta en la maestra y añadir tu UID a la colección `admins`.

Sin `google-services.json` la app compila igual y muestra en pantalla qué falta.

## Estructura

```
android/
├─ core/            Kotlin puro, sin Android: modelo, motor de etiquetas y textos. Con tests.
├─ app/src/main     Común: tema, componentes, login, acceso a Firebase.
├─ app/src/cliente  Pantallas de la app de cliente.
├─ app/src/maestra  Pantallas de la maestra, cámara y carta de ejemplo.
└─ firebase/        Reglas de seguridad de Firestore y Storage.
```

Pruebas del motor: `./gradlew :core:test`.

### Datos

```
productos/{id}                nombre, distribuidor, molecula, tipo, detalle, fotoUrl,
                              formatos [{g, precio}], declarada, publicado, creado
productos/{id}/resenas/{uid}  uid, alias, texto, estrellas, potencia, fecha
admins/{uid}                  (vacío) — permiso de maestra
```

Las etiquetas **no se guardan**: cada app las calcula con `core/Etiquetas.kt` a partir del texto de las valoraciones, igual que el prototipo (sinónimos, negaciones en las 4 palabras anteriores, se publican con 4 personas distintas). Si cambias los sinónimos, se aplican a todo lo ya escrito.

## Diferencias con el prototipo

Lo que cambia respecto a `Catanna Social.dc.html` y por qué:

- **Hay que entrar con cuenta** (correo y contraseña). El prototipo no tenía login, pero la app es privada y hace falta saber quién valora para firmar con iniciales y para que cada persona cuente una sola vez en el motor de etiquetas.
- **Sin QR del ticket ni «compra verificada».** Sin la tienda no hay forma de verificar compras: cualquiera con cuenta puede valorar cualquier producto. El botón ⌗ de la cabecera pasa a ser tus iniciales (cerrar sesión).
- **Una valoración por persona y producto.** Si vuelves a valorar, sustituye a la anterior (la pantalla te lo avisa y viene rellena).
- **Probados** muestra lo que has valorado; desaparece la tarjeta «Compraste X».
- **Hay que elegir estrellas y potencia** para publicar (el prototipo ponía 4 y 3 por defecto).
- **Los chips de «A qué te ha recordado» cuentan** aunque escribas texto: se añaden como frase aparte para que el motor los sume. En el prototipo solo contaban si el texto estaba vacío.
- **Sin foto se ve la inicial**, no la foto de Static (en el chat se vio que confundía).
- **En la maestra:** la foto se hace con la cámara o se elige de la galería; hay campos opcionales de *detalle* y *potencia que pone la bolsa* (los muestra la ficha); se pueden añadir tamaños; *Guardar borrador* guarda sin publicar; tocar un producto del catálogo lo abre para editarlo o retirarlo de la app.

## Pendiente

- Moderar valoraciones desde la maestra (hoy se borran desde la consola de Firebase).
- Editar los sinónimos de las etiquetas desde la maestra (hoy están en `core/Etiquetas.kt`).
- Cualquiera con el APK puede crear cuenta en la app de cliente. Si quieres restringirlo, se puede pasar a invitaciones.
