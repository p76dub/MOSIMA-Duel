use_module(library(jpl)).

shotImpact(Probability):-
	Probability>0.1.

/* DECISIONS */

explore(EnemyInSight):-
	not(EnemyInSight).

toOpenFire(EnemyInSight,P):-
	shotImpact(P),
	EnemyInSight.

attack(EnemyInSight):-
	EnemyInSight.