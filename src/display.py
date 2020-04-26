import pygame
import os
import sys

path = os.getcwd()
path1 = path + "/instances/Instances/150.30.1.txt"
path2 = path + "/vehicleRoute.txt"
path3 = path + "/dronesRoute.txt"

pos = [[500, 500]]
with open(path1, "r") as f:
    for line in f.readlines()[2:]:
        lineList = line.split("  ")
        tempPos = [int(20*float(lineList[0])) + 500, int(20*float(lineList[1])) + 500]
        pos.append(tempPos)

vehicleRoute = []
with open(path2, "r") as f:
    for line in f.readlines():
        line.strip()
        route = []
        lineList = line.split(" ")
        temp = [int(x) for x in lineList[:-1]]
        vehicleRoute.append(temp)

droneRoute = []
with open(path3, "r") as f:
    for line in f.readlines():
        line.strip()
        lineList = line.split(" ")
        temp = [int(x) for x in lineList]
        droneRoute.append(temp)

pygame.init()
screencaption = pygame.display.set_caption('Solution Display')
screen = pygame.display.set_mode([1600, 1600])
screen.fill([255, 255, 255])

for route in vehicleRoute:
    Route = []
    for poi in route:
        pygame.draw.circle(screen, [0, 0, 0], pos[poi], 4, 1)
        Route.append(pos[poi])
    pygame.draw.lines(screen, [0, 0, 0], True, Route, 1)

for poi in droneRoute:
    Route = []
    pygame.draw.circle(screen, [0, 0, 0], pos[poi[1]], 4, 1)
    for i in poi:
        Route.append(pos[i])
    pygame.draw.lines(screen, [0, 255, 0], False, Route, 1)

pygame.display.flip()
while True:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            sys.exit()
