# CFDSolver
### Computational Fluid Dynamics Solver

![Build Status](https://github.com/heySourabh/CFDSolver/actions/workflows/maven.yml/badge.svg)

This is a code for solving partial differential equations of the form resembling Navier-Stokes equations.

Most of the details of the numerical calculations in this solver are presented in our paper:  
[Computers & Fluids, Volume 244, 15 August 2022, 105570](https://doi.org/10.1016/j.compfluid.2022.105570)

Have a look at the [Wiki Page](https://github.com/heySourabh/CFDSolver/wiki) for further details.

#### Developer:
Sourabh Bhat (heySourabh@gmail.com)

--------------
### Some results:
#### Supersonic flow at Mach 2 over diamond-shaped airfoil, with mesh adaptation (using [MMG](https://github.com/MmgTools/mmg)) 
![Supersonic flow over diamond airfoil](docs/results/mesh_adaptation_diamond_airfoil.png)

--------------
#### Grain-Growth in Metal Solidification (Fan-Chen Model):
![Grain-Growth](docs/results/grain-growth.gif)

--------------
#### Non-axisymmetric bubble rise problem:
![Non-axisymmetric bubble rise](docs/results/non-axisymmetric_bubble_rise.png)   
![Non-axisymmetric bubble animation](docs/results/merging_bubbles.gif)

--------------
#### Sloshing:
![Sloshing](docs/results/sloshing.gif)

--------------
#### Dam break problem:
![Dam break](docs/results/dam_dreak.png)   

--------------
#### Drop splash problem:
![Drop splash](docs/results/drop_splash.png)

--------------
#### Karman Vortex Shedding:
![Vortex shedding](docs/results/karman-vortex.gif)

--------------
#### Rayleigh-Taylor Instability:
![Rayleigh-Taylor Instability](docs/results/RT.gif)