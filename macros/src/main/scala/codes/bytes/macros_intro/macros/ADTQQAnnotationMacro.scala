package codes.bytes.macros_intro.macros

import scala.annotation.{ compileTimeOnly, StaticAnnotation }
import scala.language.postfixOps
import scala.reflect.macros.whitebox

import scala.language.experimental.macros

object ADT_QQ {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    import Flag._

    val p = c.enclosingPosition

    val inputs = annottees.map(_.tree).toList

    val result: Tree = inputs match {
      case (t @ q"$mods trait $name[..$tparams] extends ..$parents { ..$body }") :: Nil if mods.hasFlag(SEALED) ⇒
        c.info(p, s"ADT Root trait $name sanity checks OK.", force = true)
        t
      case (t @ q"$mods trait $name[..$tparams] extends ..$parents { ..$body }") :: Nil ⇒
        c.abort(p, s"ADT Root traits (trait $name) must be sealed.")
      case (cls @ q"$mods class $name[..$tparams] extends ..$parents { ..$body }") :: Nil if mods.hasFlag(ABSTRACT) && mods.hasFlag(SEALED) ⇒ // there's no bitwise AND (just OR) on Flags
        c.info(p, s"ADT Root class $name sanity checks OK.", force = true)
        cls
      case (cls @ q"$mods class $name[..$tparams] extends ..$parents { ..$body }") :: Nil ⇒
        c.abort(p, s"ADT Root classes (class $name) must be abstract and sealed.")
      case (o @ q"$mods object $name") :: Nil ⇒
        c.abort(p, s"ADT Roots (object $name) may not be Objects.")
      // companions
      case (t @ q"$mods trait $name[..$tparams] extends ..$parents { ..$body }") :: (mD: ModuleDef):: Nil if mods.hasFlag(SEALED) ⇒
        c.info(p, s"ADT Root trait $name sanity checks OK.", force = true)
        q"$t; $mD"
      case (t @ q"$mods trait $name[..$tparams] extends ..$parents { ..$body }") :: (mD: ModuleDef) :: Nil ⇒
        c.abort(p, s"ADT Root traits (trait $name) must be sealed.")
      case (cls @ q"$mods class $name[..$tparams] extends ..$parents { ..$body }") :: (mD: ModuleDef) :: Nil⇒ // there's no bitwise AND (just OR) on Flags
        c.info(p, s"ADT Root class $name sanity checks OK.", force = true)
        q"$cls; $mD"
      case (cls @ q"$mods class $name[..$tparams] extends ..$parents { ..$body }") :: (mD: ModuleDef) :: Nil ⇒
        c.abort(p, s"ADT Root classes (class $name) must be abstract and sealed.")
      // method definition
      case (d @ q"def $name = $body") :: Nil ⇒
        c.abort(p, s"ADT Roots (def $name) may not be Methods.")
      // immutable variable definition
      case (v @ q"val $name = $value") :: Nil ⇒
        c.abort(p, s"ADT Roots (val $name) may not be Variables.")
      case (v @ q"var $name = $value") :: Nil ⇒
        c.abort(p, s"ADT Roots (var $name) may not be Variables.")
      // I checked and you cannot annotate a package object at all
      case x :: Nil ⇒
        c.abort(p, s"! Invalid ADT Root ($x) ${x.getClass}")
      case Nil ⇒
        c.abort(p, s"Cannot ADT Validate an empty Tree.")
    }

    c.Expr[Any](result)
  }

}

/**
 * From the Macro Paradise Docs...
 *
 * note the @compileTimeOnly annotation. It is not mandatory, but is recommended to avoid confusion.
 * Macro annotations look like normal annotations to the vanilla Scala compiler, so if you forget
 * to enable the macro paradise plugin in your build, your annotations will silently fail to expand.
 * The @compileTimeOnly annotation makes sure that no reference to the underlying definition is
 * present in the program code after typer, so it will prevent the aforementioned situation
 * from happening.
 */
@compileTimeOnly("Enable Macro Paradise for Expansion of Annotations via Macros.")
final class ADT_QQ extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ADT_QQ.impl
}
// vim: set ts=2 sw=2 sts=2 et:
