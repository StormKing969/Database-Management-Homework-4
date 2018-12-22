select f.*, f.arrives-f.departs as delta from flights f where f.origin = 'Los-Angeles' and f.destination = 'Honolulu';
