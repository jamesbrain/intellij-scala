package org.jetbrains.plugins.scala.lang.psi.types

import api.statements._
import params._
import resolve.ScalaResolveResult
import api.toplevel.typedef.ScTypeDefinition
import impl.toplevel.typedef.TypeDefinitionMembers
import _root_.scala.collection.immutable.HashSet

import com.intellij.psi._

object Conformance {
  def conforms (l : ScType, r : ScType) : boolean = conforms(l, r, HashSet.empty)

  private def conforms (l : ScType, r : ScType, visited : Set[PsiClass]) : Boolean = {
    if (l equiv r) true
    else l match {
      case Any => true
      case Nothing => false
      case Null => r == Nothing
      case AnyRef => r match {
        case Null => true
        case _: ScParameterizedType => true
        case _: ScDesignatorType => true
        case _: ScSingletonType => true
        case _ => false
      }
      case Singleton => r match {
        case _: ScSingletonType => true
        case _ => false
      }
      case AnyVal => r match {
        case _: ValType => true
        case _ => false
      }
      case ValType(_, tSuper) => tSuper match {
        case Some(tSuper) => conforms(l, tSuper)
        case _ => false
      }
      case ScDesignatorType(tp : ScTypeParam) => conforms(tp.lowerBound, r)
      case ScDesignatorType(tp : PsiTypeParameter) => r == Nothing //Q: what about AnyRef?
      case ScTypeAliasDesignatorType(ta, subst) => conforms(subst.subst(ta.lowerBound), r)

      case ScParameterizedType(ScDesignatorType(owner : PsiClass), args1) => r match {
        case ScParameterizedType(ScDesignatorType(owner1 : PsiClass), args2) if (owner == owner1) =>
          owner.getTypeParameters.equalsWith(args1 zip args2) {
            (tp, argsPair) => tp match {
              case scp : ScTypeParam if (scp.isCovariant) => if (!argsPair._1.conforms(argsPair._2)) return false
              case scp : ScTypeParam if (scp.isContravariant) => if (!argsPair._2.conforms(argsPair._1)) return false
              case _ => if (!argsPair._1.equiv(argsPair._2)) return false
            }
            true
          }
        case _ => rightRec(l, r, visited)
      }

      case c@ScCompoundType(comps, decls, types) => comps.forall(_ conforms r) && (extractClassType(r) match {
        case Some((clazz, subst)) => {
          if (!decls.isEmpty) {
            val sigs = TypeDefinitionMembers.getSignatures(clazz)
            for (sig <- c.signatureSet) {
              sigs.get(sig) match {
                case None => return false
                case Some(retType) => if (!subst.subst(retType).conforms(sig.retType)) return false
              }
            }
          }
          if (!types.isEmpty) {
            val hisTypes = TypeDefinitionMembers.getTypes(clazz)
            for (t <- types) {
              hisTypes.get(t) match {
                case None => return false
                case Some(n) => {
                  val subst1 = n.substitutor
                  n.info match {
                    case ta: ScTypeAlias => if (!subst.subst(subst1.subst(ta.upperBound)).conforms(t.upperBound) ||
                            !t.lowerBound.conforms(subst.subst(subst1.subst(ta.lowerBound)))) return false
                    case inner: PsiClass => //todo: java inner class case
                  }
                }
              }
            }
          }
          true
        }
        case None => false
      })

      case _ => rightRec(l, r, visited)
    }
  }

  private def rightRec(l: ScType, r: ScType, visited : Set[PsiClass]) : Boolean = r match {
    case ScSingletonType(path) => path.bind match {
      case Some(ScalaResolveResult(e, _)) => conforms(l, new ScDesignatorType(e))
      case _ => false
    }
    case ScDesignatorType(tp: ScTypeParam) => conforms(l, tp.upperBound)

    case ScDesignatorType(td: ScTypeDefinition) => !td.superTypes.find {t => conforms(l, t, visited + td)}.isEmpty
    case ScDesignatorType(clazz: PsiClass) =>
    !clazz.getSuperTypes.find {t => conforms(l, ScType.create(t, clazz.getProject), visited + clazz)}.isEmpty

    case ScTypeAliasDesignatorType(ta, subst) => conforms(l, subst.subst(ta.upperBound))

    case p@ScParameterizedType(ScDesignatorType(td: ScTypeDefinition), _) => {
      val s = p.substitutor
      !td.superTypes.find {t => conforms(l, s.subst(t), visited + td)}.isEmpty
    }
    case p@ScParameterizedType(ScDesignatorType(clazz: PsiClass), _) => {
      val s = p.substitutor
      !clazz.getSuperTypes.find {t => conforms(l, s.subst(ScType.create(t, clazz.getProject)), visited + clazz)}.isEmpty
    }

    case ScCompoundType(comps, _, _) => !comps.find(l conforms _).isEmpty

    case _ => false //todo
  }

  private def extractClassType(t : ScType) = t match {
    case ScDesignatorType(clazz : PsiClass) => Some(clazz, ScSubstitutor.empty)
    case p@ScParameterizedType(ScDesignatorType(clazz : PsiClass), _) => Some(clazz, p.substitutor)
    case _ => None //todo
 }
}