use_module(library(jpl)).

shotImpact(Probability):-
	Probability>0.1.

/* DECISIONS */

explore(Player):-
	not(see(Player)).

toOpenFire(Player,P):-
	shotImpact(P),
	see(Player).

attack(Player):-
	see(Player).

see(Player):-
    jpl_call(Player, enemyInSight, [], X),
    jpl_is_true(X).

