# Experimental native formula verification

42 publications, 23802 frame positions; maximum rounded residual 1 twip(s). 6 publications are held-out strength 6 with a different length/frame rate.

Bounce uses frame-rounded segment widths; a duration-independent harmonic formula fails these samples. Spring uses an initial sine rise followed by a damped cosine about 0.7. Only measured positive integer strengths are validated. Random is excluded; repeatability does not establish its PRNG or arbitrary-strength formula. Exporter support is unchanged.

| Case | Role | Publication | Compared frames | Max rounded error (twips) | Max raw error (px) |
| --- | --- | --- | ---: | ---: | ---: |
| bounce_in_s1_601 | discovery | imported | 601 | 1 | 0.0500000000001819 |
| bounce_in_s1_601 | discovery | persisted | 601 | 1 | 0.05000000000009095 |
| bounce_in_s2_601 | discovery | imported | 601 | 1 | 0.05100665484678757 |
| bounce_in_s2_601 | discovery | persisted | 601 | 1 | 0.0500000000001819 |
| bounce_in_s3_601 | discovery | imported | 601 | 1 | 0.05119939399041869 |
| bounce_in_s3_601 | discovery | persisted | 601 | 1 | 0.0512311907901676 |
| bounce_in_s4_601 | discovery | imported | 601 | 1 | 0.0515625 |
| bounce_in_s4_601 | discovery | persisted | 601 | 1 | 0.0515625 |
| bounce_in_s5_601 | discovery | imported | 601 | 1 | 0.05075684193786856 |
| bounce_in_s5_601 | discovery | persisted | 601 | 1 | 0.051179719238461985 |
| bounce_in_s6_validation | validation | imported | 361 | 1 | 0.05260076819831738 |
| bounce_in_s6_validation | validation | persisted | 361 | 1 | 0.052350872321676435 |
| bounce_in_s8_601 | discovery | imported | 601 | 1 | 0.050173010380603955 |
| bounce_in_s8_601 | discovery | persisted | 601 | 1 | 0.050173010380603955 |
| bounce_s1_601 | discovery | imported | 601 | 1 | 0.0500000000001819 |
| bounce_s1_601 | discovery | persisted | 601 | 1 | 0.0500000000001819 |
| bounce_s2_601 | discovery | imported | 601 | 1 | 0.050646759727897006 |
| bounce_s2_601 | discovery | persisted | 601 | 1 | 0.051587346179030644 |
| bounce_s3_601 | discovery | imported | 601 | 1 | 0.05153300943029535 |
| bounce_s3_601 | discovery | persisted | 601 | 1 | 0.05153300943029535 |
| bounce_s4_601 | discovery | imported | 601 | 1 | 0.051462427459523494 |
| bounce_s4_601 | discovery | persisted | 601 | 1 | 0.051462427459523494 |
| bounce_s5_601 | discovery | imported | 601 | 1 | 0.051462427459523494 |
| bounce_s5_601 | discovery | persisted | 601 | 1 | 0.051462427459523494 |
| bounce_s6_validation | validation | imported | 361 | 1 | 0.05247463824900933 |
| bounce_s6_validation | validation | persisted | 361 | 1 | 0.05247463824900933 |
| bounce_s8_601 | discovery | imported | 601 | 1 | 0.05113733921932635 |
| bounce_s8_601 | discovery | persisted | 601 | 1 | 0.05137178890618088 |
| spring_s1_601 | discovery | imported | 601 | 1 | 0.051618663402382484 |
| spring_s1_601 | discovery | persisted | 601 | 1 | 0.051618663402382484 |
| spring_s2_601 | discovery | imported | 601 | 1 | 0.051618663402382484 |
| spring_s2_601 | discovery | persisted | 601 | 1 | 0.051618663402382484 |
| spring_s3_601 | discovery | imported | 601 | 1 | 0.05131373406748026 |
| spring_s3_601 | discovery | persisted | 601 | 1 | 0.05 |
| spring_s4_601 | discovery | imported | 601 | 1 | 0.051118389388102516 |
| spring_s4_601 | discovery | persisted | 601 | 1 | 0.05 |
| spring_s5_601 | discovery | imported | 601 | 1 | 0.051664944339790964 |
| spring_s5_601 | discovery | persisted | 601 | 1 | 0.05004792123345396 |
| spring_s6_validation | validation | imported | 361 | 1 | 0.0516553366500375 |
| spring_s6_validation | validation | persisted | 361 | 1 | 0.05 |
| spring_s8_601 | discovery | imported | 601 | 1 | 0.05151819656512089 |
| spring_s8_601 | discovery | persisted | 601 | 1 | 0.05 |
