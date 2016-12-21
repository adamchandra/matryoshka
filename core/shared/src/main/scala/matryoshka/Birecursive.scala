/*
 * Copyright 2014–2016 SlamData Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package matryoshka

import scalaz._

/** A type that is both [[Recursive]] and [[Corecursive]].
  *
  * NB: Do not define instances of this or use it as a constraint until issue
  *     #44 is resolved. Define an instance of [[BirecursiveT]] if possible,
  *     otherwise define separate [[Recursive]] and [[Corecursive]] instances.
  */
// TODO: Once this type class is actually usable, operations in `Recursive` and
//       `Corecursive` that also have a `Corecursive.Base[T, Base]` constraint
//       should be moved here.
trait Birecursive[T] extends Recursive[T] with Corecursive[T]

object Birecursive {
  /** Create a [[Birecursive]] instance from the mappings to/from the
    * fixed-point.
    */
  def algebraIso[T, F[_]](φ: Algebra[F, T], ψ: Coalgebra[F, T])
      : Birecursive.Aux[T, F] =
    new Birecursive[T] {
      type Base[A] = F[A]
      def project(t: T)(implicit F: Functor[F]) = ψ(t)
      def embed(ft: F[T])(implicit F: Functor[F]) = φ(ft)
    }

  // NB: The rest of this is what would be generated by simulacrum, except this
  //     type class is too complicated to take advantage of that.

  type Aux[T, F[_]] = Birecursive[T] { type Base[A] = F[A] }

  def apply[T, F[_]](implicit instance: Aux[T, F]): Aux[T, F] = instance

  trait Ops[T, F[_]] {
    def typeClassInstance: Aux[T, F]
    def self: T
  }

  trait ToBirecursiveOps {
    implicit def toBirecursiveOps[T, F[_]](target: T)(implicit tc: Aux[T, F])
        : Ops[T, F] =
      new Ops[T, F] {
        val self = target
        val typeClassInstance = tc
      }
  }

  object nonInheritedOps extends ToBirecursiveOps

  trait AllOps[T, F[_]] extends Ops[T, F] with Recursive.Ops[T, F] {
    def typeClassInstance: Aux[T, F]
  }

  object ops {
    implicit def toAllBirecursiveOps[T, F[_]](target: T)(implicit tc: Aux[T, F])
        : AllOps[T, F] =
      new AllOps[T, F] {
        val self = target
        val typeClassInstance = tc
      }
  }
}
