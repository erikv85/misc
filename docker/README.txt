# General instructions

# Installing Docker:

# (see online docs)

# Build and use:

docker build -t glonk .
docker run --name=fnork -di glonk
docker exec -it fnork bash

# Root stuff:

# Enter container as root:
docker exec -u root -it fnork bash

# or visudo and set this:
# %sudo   ALL=(ALL:ALL) NOPASSWD:ALL
