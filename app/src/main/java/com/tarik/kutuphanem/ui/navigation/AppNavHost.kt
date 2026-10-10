package com.tarik.kutuphanem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tarik.kutuphanem.ui.auth.GirisEkrani
import com.tarik.kutuphanem.ui.auth.KayitEkrani
import com.tarik.kutuphanem.ui.books.KitapListesiEkrani
import com.tarik.kutuphanem.ui.detail.KitapDetayEkrani
import com.tarik.kutuphanem.ui.libraries.KutuphanelerEkrani
import com.tarik.kutuphanem.ui.manual.KitapFormEkrani
import com.tarik.kutuphanem.ui.scanner.TarayiciEkrani
import com.tarik.kutuphanem.ui.search.IsimleAramaEkrani

@Composable
fun AppNavHost(
    girisYapilmis: Boolean,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = if (girisYapilmis) KutuphanelerRoute else GirisRoute
    ) {
        composable<GirisRoute> {
            GirisEkrani(onKayitaGit = { navController.navigate(KayitRoute) })
        }

        composable<KayitRoute> {
            KayitEkrani(onGeri = { navController.popBackStack() })
        }

        composable<KutuphanelerRoute> {
            KutuphanelerEkrani(
                onKutuphaneAc = { kutuphane ->
                    navController.navigate(
                        KitapListesiRoute(kutuphaneId = kutuphane.id, kutuphaneAdi = kutuphane.ad)
                    )
                },
                onKitapAc = { sonuc ->
                    navController.navigate(
                        KitapDetayRoute(kutuphaneId = sonuc.kutuphaneId, kitapId = sonuc.kitap.id)
                    )
                }
            )
        }

        composable<KitapListesiRoute> { entry ->
            val rota = entry.toRoute<KitapListesiRoute>()
            KitapListesiEkrani(
                onGeri = { navController.popBackStack() },
                onKitapAc = { kitap ->
                    navController.navigate(
                        KitapDetayRoute(kutuphaneId = rota.kutuphaneId, kitapId = kitap.id)
                    )
                },
                onBarkodTara = { navController.navigate(TarayiciRoute(rota.kutuphaneId)) },
                onElleEkle = { navController.navigate(KitapFormRoute(kutuphaneId = rota.kutuphaneId)) },
                onIsimleAra = { navController.navigate(IsimleAramaRoute(rota.kutuphaneId)) }
            )
        }

        composable<TarayiciRoute> { entry ->
            val rota = entry.toRoute<TarayiciRoute>()
            TarayiciEkrani(
                onGeri = { navController.popBackStack() },
                onIsbnOkundu = { isbn ->
                    // Tarayici geri yiginda kalmasin: kaydettikten sonra dogrudan listeye donulsun.
                    navController.navigate(
                        KitapFormRoute(kutuphaneId = rota.kutuphaneId, isbn = isbn)
                    ) {
                        popUpTo(TarayiciRoute(rota.kutuphaneId)) { inclusive = true }
                    }
                },
                onElleGir = {
                    navController.navigate(KitapFormRoute(kutuphaneId = rota.kutuphaneId)) {
                        popUpTo(TarayiciRoute(rota.kutuphaneId)) { inclusive = true }
                    }
                }
            )
        }

        composable<IsimleAramaRoute> {
            IsimleAramaEkrani(
                onGeri = { navController.popBackStack() },
                onEklendi = { navController.popBackStack() }
            )
        }

        composable<KitapFormRoute> { entry ->
            val rota = entry.toRoute<KitapFormRoute>()
            KitapFormEkrani(
                onGeri = { navController.popBackStack() },
                onKaydedildi = { navController.popBackStack() },
                onIsimleAra = { navController.navigate(IsimleAramaRoute(rota.kutuphaneId)) }
            )
        }

        composable<KitapDetayRoute> { entry ->
            val rota = entry.toRoute<KitapDetayRoute>()
            KitapDetayEkrani(
                onGeri = { navController.popBackStack() },
                onDuzenle = { kitapId ->
                    navController.navigate(
                        KitapFormRoute(kutuphaneId = rota.kutuphaneId, kitapId = kitapId)
                    )
                }
            )
        }
    }
}
