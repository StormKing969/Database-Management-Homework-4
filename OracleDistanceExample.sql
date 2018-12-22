
-- from https://community.oracle.com/thread/2569842
-- result: a little over 1352 meters 
--  DISTANCE_M
--  --------
--  1352.24554

select sdo_geom.sdo_distance (
         sdo_geometry (
            -- this identifies the object as a two-dimensional point.
             2001,
             -- this identifies the object as using the GCS_WGS_1984 geographic coordinate system.
             4326, null, sdo_elem_info_array(1, 1, 1),
             -- this is the longitude and latitude of point 1.
             sdo_ordinate_array(151.20208, -33.883741)
          ),
	  sdo_geometry (
             2001,
             4326, null, sdo_elem_info_array(1, 1, 1),
             -- this is the longitude and latitude of point 2.
             sdo_ordinate_array(151.195986, -33.87266)
          ),  1,  'unit=M'
       ) distance_m from dual;
