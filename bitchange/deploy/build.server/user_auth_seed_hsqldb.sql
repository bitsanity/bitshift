--------------------------------------------------------
--  File created - Monday-September-14-2015   
--
-- add as MAVEN target?  https://bodez.wordpress.com/resources/maven-tips-tricks/run-sqlplus-with-the-exec-maven-plugin/
--------------------------------------------------------
--REM INSERTING into USER_AUTHORIZATION

--Delete all data if data exists
TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK;

--------------------
-- Server keys
--------------------
-- server public key, 4096 key size
Insert into USER_AUTHORIZATION (CLIENT_ID,KEY_TYPE,CREATED_ON,EXPIRATION,DESCRIPTION,ASYM_KEY) values ('PR_SERVER','B',CURRENT_TIMESTAMP, to_timestamp('11-09-2020 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),'Predictive Rates Server Unit Test','30820222300d06092a864886f70d01010105000382020f003082020a02820201008c7c0afc250b2d00747d6aadf7f86ddc3ba0a631640c05d8f4da990dd8118679de0941e856424ecdd8e687661171464a2240cc0db1593eadac4e18b4e17b73bd463a8e3c0693b96b675c2a3c978174c37fcbfcfa5451575503276b458992cd629f7b3e49f4ee6169335dee6e875e850caa40aba244baed1fbdddc3861e319aa70d538a549b80334179211762d29ae9b453c8f5362f7e6d4a74184b510cb8c2438057ab1668f1293b43d456d740a9fbebb935acc0617f10bf92325e6e84ffe5898014a5e0d2463e46e4ed2e8bef2379328045238e31e712bfa14d118c759aaccfa96352378db97917cb3fa42ef5f48bc0261c77433649bfa71d2c0152c4a8823b4410c3d6a376fdb9c523af7a3615ebcb43fcc952949a1240f81dd718a32e326b747f26fd45294bc134f0968fd13131f65e9df3128a30d09f2615b8898dd05d3bf3d875a85583506db0b43b4d79fee8a48c1c79bcdb32d6cec1ed4d8e7f3f17666b8ec8cca08bd43261341e3f48e9627bc1bb955c46c37b77662cab3afc867bd987ac19c7f8ac6fc2b1520d07aa2b6df9d3e9029497d22d651990fd1ce69bfbfe3759935c50df86f523a5396652dc52d00d2012df68a90a75b76ddafc359abcd044f49b19be316ee9bb8390d7135f24013ff25f568460e71ed340de46053f2c3c8e8954c9d248e4db01c108f8afa38c6ff47207e0e9b4d2663673da00bc651a710203010001');


-- server private key
Insert into USER_AUTHORIZATION (CLIENT_ID,KEY_TYPE,CREATED_ON,EXPIRATION,DESCRIPTION,ASYM_KEY) values ('PR_SERVER','V',CURRENT_TIMESTAMP, to_timestamp('11-09-2020 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),'Predictive Rates Server Unit Test','30820942020100300d06092a864886f70d01010105000482092c3082092802010002820201008c7c0afc250b2d00747d6aadf7f86ddc3ba0a631640c05d8f4da990dd8118679de0941e856424ecdd8e687661171464a2240cc0db1593eadac4e18b4e17b73bd463a8e3c0693b96b675c2a3c978174c37fcbfcfa5451575503276b458992cd629f7b3e49f4ee6169335dee6e875e850caa40aba244baed1fbdddc3861e319aa70d538a549b80334179211762d29ae9b453c8f5362f7e6d4a74184b510cb8c2438057ab1668f1293b43d456d740a9fbebb935acc0617f10bf92325e6e84ffe5898014a5e0d2463e46e4ed2e8bef2379328045238e31e712bfa14d118c759aaccfa96352378db97917cb3fa42ef5f48bc0261c77433649bfa71d2c0152c4a8823b4410c3d6a376fdb9c523af7a3615ebcb43fcc952949a1240f81dd718a32e326b747f26fd45294bc134f0968fd13131f65e9df3128a30d09f2615b8898dd05d3bf3d875a85583506db0b43b4d79fee8a48c1c79bcdb32d6cec1ed4d8e7f3f17666b8ec8cca08bd43261341e3f48e9627bc1bb955c46c37b77662cab3afc867bd987ac19c7f8ac6fc2b1520d07aa2b6df9d3e9029497d22d651990fd1ce69bfbfe3759935c50df86f523a5396652dc52d00d2012df68a90a75b76ddafc359abcd044f49b19be316ee9bb8390d7135f24013ff25f568460e71ed340de46053f2c3c8e8954c9d248e4db01c108f8afa38c6ff47207e0e9b4d2663673da00bc651a710203010001028202002df334b49c793752fac73ae9843e21d0f33fec9c23193ba0671649119a26c151832a8c002e96a9a7f2d6145b724116b890d398eab1a37b38db2f002dc9c2d36c895805c5e470d5292839a12708773375ee8b0e47e2e468284a4bce6e843d1c28b1f961ba56fed1a5d3435a059509798356fca826ff4c14db5902fc3d160b2b8690794eb9b5d30cf0d169dd7859628a268d7121a3838d5aba0b9716ab7ff9beb0cd7ddf5c091f3859462a61ed2e1d6b6502c367a9d539f68472913f09ef3eb41971674f8a65bccc09ec3f18067545a912b799e5b350e73fdea627cb21a225e1785af33da5261a105d940150f67b446da17fd5b7bca8391f12ce1be2a2ebe822103afd6831054ab17f8512250b52795ebdefb3413c37477ef9a0e8b4f616190893af9dbf709200298460789c2a4d3c55cfcdcc6a9ec7e4d358b53d08d1b5c18c46343227da6ca8f65c73b92c05fdfd2c7b9997f87a195150f93e041cc7d5cbf5fbef8e18158d292883e7c75c19cac5084a922a9e008a4ec28721de650732465c64077d63f81fee50b881c7065c09dc275bfb270ba80f0672f067cf824dab06b55919008aaea8818d405e424e2585603a8772e26446edb0d9a24e857ab40461468e542357671d0bd6ba4ac0323464f14746844cc90ed6120cc47b53b0072db9e012aa73555e25c78282f115f9a08e55e32065a3407887b77f3411acf89e48c3b3710282010100dd46be020c8d1c74e0b0e1490eee74e41334926961b53dd094fd9d4bd5d91f3339d0e9d482284bfd58dd6111dcd0b6a30ee3bad02ee0d3922a7632ee975ed3ae094b6cd69b8f363b4f5a46b5a351ef59aec465b4d4a5636ba9950743a23fe6fc34f148e85afb9480d8ad3a638e4d8f4a96c46a18ca677e5c74d62677c89831df72011331d9677ebf1c868f659fddcb8e4849f030a8cdc3dd68a761598dae78db4e25fd33dcbfb2a6676b78ba652f6206c81701c69807ca5eb6625e94789241791882cf41404036ee4e6101c82fb12f07797b057c7e687e0ce8f3a26a770ce2e9d1c161dfe2e5da376c316564c95bddca971b346a978be5324a7ba2c4535d7abf0282010100a287ae0c3955dad76b9e340535cfb23de97274bb5556ecfabb172cc5ef363b3e0e0bd4f1e78a0ea9b4a72537247f667efa5c0f546e791470939b5760fc9d5db451590f5b588ebef06286b8141089056ab3d6431ba33ed76f56186b99fcd06cbff13a926e43c37c71c1b76d0a517b10de35a6dd8d3d37321b82e00fd99d882c23ca142c97e40d9170a8878f5deade448cdbe303e102f48772a3ae6562faac40fa228dc7c0c1209299c9b66d8183a71d1ee7933a5ac310d97124f87575efe492465fc7060c5131b605a1a7038ff971558582051dcf2463ab0edb58fe3331519a24ccdb0f20d548dc2d5ba71c06120e57330aa73ee656efc237042d36937a89a6cf02820100012e85894143e7a9002231b1d03099d335ff0be1203275980d93a5296884f653b1b559ea569f9c7847e736c37fa865b14d3d0e1c5a83bcdd84f888a11991da138ddd2741a62707a51d715ffb4c59fdab3ef1b1915ba95e748ebd4b02a3752fe89ce7c4f9e8b27b428ce5aff0242c69dd7e1ca26c6b784e06746148674f5cf0b73ec69eb19f759c09ad3f52b8ba37d70475825f9402f2c65f0256128105eeae9a8d3a4ab5e231ebe4539d1151fd845943ac01e3728a1150955ce7fd955167868dfd0185d5d560026b1764113c5e74d5c9766e9fc2e071f26a633c19dbf3b2cfeb6bc15aa27575c0ca96fb548d4b3e005b3a9f5c924d28e41a76a75e612a08cfc5028201005281980c228affe5fd7eb91faffa789c4f46c81555342466ffd4bae8dc0f66b190c5d6dada544e2f3e42df7487b598dd7ad08303b47508b84166494c35d0d901b18e096ab0a0fe32df814e2ef959e5830e3a05a52110c4a2834d304a627997cb34752c628749d48196fa3b83526babdb71d6fc7f37852e8da2b985b24df3139d33775c4fdaf83c4db96fdf8aa8b11e91770a5e2cbaa8519e1393736a74a03ef7fbd48783c54678c5530da5b76a92ca08a73c29126c15c09c5c4d38ab9f1524604f3ca4485a6b66e0a2f83aa9bad21abd048cfe5dea36225f0f60b4e25df5f3cf1ca353a1b24423696f2e7cde640a1da00e976a29da162a3c6d855a4031a371270282010100bf491250e4f041b067f6c7cafa072bf8b070ee8dbc49bdfeb923b98a3dfad094193a2d184baf88fc8f30f3f9a47cf8a07f0552f1f41f565d6c6ae10771ea79eae6ae829edbc4b991b9e10a9f81cc0d01919c0c7ede2e5a15ad0a0436724b5fc4082b625b0e2919284f43f5699f96375ef7a0a16f255d1018553bfd1a0e1394303ac012f41d12039b636ede7256c58560dc0908ef7af970b7326845625a7cfe199457242566d0a9fe42803a1d5d0d855f43f53b492d14cebdda236a224c1be14b68b4dde40844bf646688d9ad1924a181f68c94047e5bca82016340ec421fd48da3441a0a51765fd775276d0a7c6ef492d789cc1f473442b05d49d2db07b18040');


--------------
-- Clients
--------------
--
-- 2048 key size
Insert into USER_AUTHORIZATION (CLIENT_ID,KEY_TYPE,CREATED_ON,EXPIRATION,DESCRIPTION,ASYM_KEY) values ('TestA','B',CURRENT_TIMESTAMP,
  to_timestamp('11-09-2020 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),'Test Client A',
  '30820122300d06092a864886f70d01010105000382010f003082010a0282010100ed53ab939050a8a8a0a0df3a1eb52a3590e7126a867f634819e4a7ce2b1b90f7193073f918f3a47b4200c73a002a981b35a92641fec797e6991ef94414bfc8e42f50174b97cd1f70dfcf094aa92d9cd86d411397681dbe3dbaaf9d01958745740a5ad93e8214ac9a03417f362ac27d7ef5ba2e57d0009a680b7727e59db0587ceb210d5de310b7f1b508955541fc0ad60d1387db2e5ca600efcff619ab2fdcf25c41aa9d68cbea7c53da7c6e6082dbbb4242133bac1f9f1a1fe8ff292884e07683fd3c7383a5ec7ab70cce42f6ae1f24872ca4b2d698b366e70e1b54a205d69552460a69f974b03265fa91b58428008dda8253b28e39273d3adf0a24b4cd24770203010001');


-- expired public key  
Insert into USER_AUTHORIZATION (CLIENT_ID,KEY_TYPE,CREATED_ON,EXPIRATION,DESCRIPTION,ASYM_KEY) values ('Expired','B',to_timestamp('10-09-2015 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'), 
to_timestamp('12-09-2015 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),'Expired Client', 
  '30820122300d06092a864886f70d01010105000382010f003082010a02820101008fde04f303d787cab25ef4c7843b604912836836c2ba391f7348d3743bf4243888aa0ac3ed150f563993756b459e096acaf2eff46edbf4ea1ed77f44f7d24c13251c0bc79d9be2d05eeec54cadace6d59389086d97191478172f9234583c1f47e30b8fe318f9452f0224205e6f1a28ec7acaf7e41c99cdd0991f5ca0f91e89db96d93bd5a0292933e5dd9c78d78a101e7cb0c970ccf8fb9e3187c106caddc319f5466e0ca803dc479e2730b423abc64e3e67abc57c461998d0b551cc6a634a42deb56e35321cd42dd14de7126fb20cbe136bf75bf48039049472cf688d508d3c1f9bc915e3e6507deea58ebbf3a2cfb7f90eb2b51660520b5b6c3c3dfcad3b450203010001');

-- "invalid" private key, 2048 key size
Insert into USER_AUTHORIZATION (CLIENT_ID,KEY_TYPE,CREATED_ON,EXPIRATION,DESCRIPTION,ASYM_KEY) values ('InvalidPrivate','V',CURRENT_TIMESTAMP,
  to_timestamp('11-09-2020 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),'Invalid Private Key',
  '30820122300d06092a864886f70d01010105000382010f003082010a0282010100ed53ab939050a8a8a0a0df3a1eb52a3590e7126a867f634819e4a7ce2b1b90f7193073f918f3a47b4200c73a002a981b35a92641fec797e6991ef94414bfc8e42f50174b97cd1f70dfcf094aa92d9cd86d411397681dbe3dbaaf9d01958745740a5ad93e8214ac9a03417f362ac27d7ef5ba2e57d0009a680b7727e59db0587ceb210d5de310b7f1b508955541fc0ad60d1387db2e5ca600efcff619ab2fdcf25c41aa9d68cbea7c53da7c6e6082dbbb4242133bac1f9f1a1fe8ff292884e07683fd3c7383a5ec7ab70cce42f6ae1f24872ca4b2d698b366e70e1b54a205d69552460a69f974b03265fa91b58428008dda8253b28e39273d3adf0a24b4cd24770203010001');


-- unite test entry, 2048 key size
Insert into USER_AUTHORIZATION (CLIENT_ID,KEY_TYPE,EXPIRATION,DESCRIPTION,ASYM_KEY) values ('testUpdateKey','B',
  to_timestamp('11-09-2020 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),'junit test for testUpdateKey',
  '30820122300d06092a864886f70d01010105000382010f003082010a0282010100b9fd14f408360885c68b6cda12e66552603f6205255701dd9844eaf57966d77f443171327cb1f5a374f2a25826ff81b900f92dcf755f0a68cd00ab3980707ccc242fa75842227e4e3d2d34b73d7332ed703e6485fb8b887c194460ae3888d14891912c463039aed66321861408bce562de7ac159674c268cc0433cca676f27e427a7f4e9177685401135748be86a2ba9a0ff8f4631aea3b2abb07c50647f9618056cfaa8af9bac16b0721b043807347479c236902020055e6219f112ceb9b38a8953509acfd584c461b6e6d250eaf8419aaefd27accbfe9638a411e4dea12e839e12578c227610968afa934853ee135c95470370c26b206112dd416a748a850d0203010001');


-- 4096 key
Insert into USER_AUTHORIZATION (CLIENT_ID,KEY_TYPE,CREATED_ON,EXPIRATION,DESCRIPTION,ASYM_KEY) values ('TestB','B',to_timestamp('11-09-2020 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),
  to_timestamp('11-09-2020 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),'Test Client B', 
 '30820222300d06092a864886f70d01010105000382020f003082020a0282020100827668238784ff5d12f7bd9ff5c72e2e9cbab05395a21f6afced793cf3fee306dfd58af9e6a56972b73eaf1948ff8941109098ec25b5634a13954af83da33e2ec045c3f7c2d5c8a5f9cd36c111ae807681f50ef12da21a42f228c2218a383d3a34576f824b5bbd25d00886b505353a313a69cd547b70ac010f5eb6a06c168c0db1fbfca76875be06e29dd8c429f183c02991a1b8f2c0a0210c5dadf1d32c31489ec92a88d2fc61624cb7d469fac6576f76684c053ed0fe469128b5fe186afc719ebda0df92d72a6ab408116a36eeb633fe737007accacc67641c729c470b4730f9c7b7b970a9c800129a0bff87c62b53e95e48ff0d72b394550d01622a2dda9403f2e301115bdbf480900a620d5867b56be8a69919ec472fa99d9111fa3b9da4d8f5465767c85f414bab3801b5406e4c1b58c8e49736e7105cc4abb0f12fbb492268364da6cafb460e209772e230d155b07ef0604c62876c97178c4371b9775b95beaa2a6cfe326fa8e68d69fe145102fe102906ba8a85e5fbf169c3b5d0706eeb3e64fc002ed5395951afb434f24cecd161823badeb13789a1d48854f28dd89e0c238fb00c3607f2fca2bca0cd513840414ab3e83f407741f11c5ca39efbed73d140a86fe8ff4ab01d32c32e72d1588674f8f714b6539d732ad986bb0c0015ee04ff0f7cca98b1c47ac63fad5e51b8f772283c8122e4662bac6a435f08b91d30203010001');


-- elliptic key, public
Insert into USER_AUTHORIZATION (CLIENT_ID,KEY_TYPE,CREATED_ON,EXPIRATION,DESCRIPTION,KEY_ALGORITHM,ASYM_KEY) values ('Elliptic','B',to_timestamp('11-09-2009 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),
  to_timestamp('11-09-2020 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),'Elliptic curve key', 'EC',
  '3081a7301006072a8648ce3d020106052b81040026038192000406cec2e4d453c9b3db08cf3fedcbe9be21e0bb65a6a31e095d7efa6975d8f9d68aacb9b46a8e20f8fc6c81fa9aa44b889885bc072e4e6ee452e31654f750e43983227e5f68450fee0359c33f17cb2cb2b994c4cb50e9906c27e7d199d7500d0fdf017b3ba0ffdc238fc1846e5e0a5c96b5dc9a19352873001e04a6c0eb284c86ba3a7ac89bdf6ac4859a7aad026c3c86');


---- elliptic key, private
Insert into USER_AUTHORIZATION (CLIENT_ID,KEY_TYPE,CREATED_ON,EXPIRATION,DESCRIPTION,KEY_ALGORITHM,ASYM_KEY) values ('Elliptic','V',to_timestamp('11-09-2009 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),
  to_timestamp('11-09-2020 08.44.49 PM','DD-MM-YYYY HH.MI.SS AM'),'Elliptic curve key', 'EC',
  '3065020100301006072a8648ce3d020106052b81040026044e304c02010104476ea07067bfeff449ba82ca5e5c99c6864e6ed701f1d98d7df858dc8c18a55e097bfe8eeaa1e5df228e2e5944b1361228dfd04ad5dac4de7a83037c4a71b0322e3c73df6c1c0993');

commit;