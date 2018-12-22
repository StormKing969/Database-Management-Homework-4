SELECT b.name, b.stars, b.review_count FROM yelp_db.business b, yelp_db.category c
WHERE b.id = c.business_id AND b.city = 'Las Vegas' AND b.state = 'NV' AND
c.category = 'Restaurants' AND 200 >
ST_Distance_Sphere(
point(
(SELECT a.longitude FROM yelp_db.apartments a WHERE a.listing = 25),
(SELECT a.latitude FROM yelp_db.apartments a WHERE a.listing = 25)
),
point(b.longitude, b.latitude)
)
GROUP BY b.name, b.stars, b.review_count
HAVING b.review_count > 9;
