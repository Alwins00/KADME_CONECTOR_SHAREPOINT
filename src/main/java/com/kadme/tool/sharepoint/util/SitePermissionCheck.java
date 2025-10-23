/* Decompiler 17ms, total 148ms, lines 13 */
package com.kadme.tool.sharepoint.util;

import java.util.Arrays;
import java.util.List;

public class SitePermissionCheck {
   private static final List<String> FULL_ACCESS_SITE_STRINGS = Arrays.asList("الجميع باستثناء المستخدمين الخارجيين", "Guztiak kanpoko erabiltzaileak izan ezik", "Всички освен външни потребители", "Tothom excepte els usuaris externs", "除外部用户外的任何人", "外部使用者以外的所有人", "Svi osim vanjskih korisnika", "Všichni kromě externích uživatelů", "Alle undtagen eksterne brugere", "Iedereen behalve externe gebruikers", "Everyone except external users", "Kõik peale väliskasutajate", "Kaikki paitsi ulkoiset käyttäjät", "Tout le monde sauf les utilisateurs externes", "Todo o mundo excepto os usuarios externos", "Jeder, außer externen Benutzern", "Όλοι εκτός από εξωτερικούς χρήστες", "כולם פרט למשתמשים חיצוניים", "बाह्य उपयोगकर्ताओं को छोड़कर सभी", "Mindenki, kivéve külső felhasználók", "Semua orang kecuali pengguna eksternal", "Tutti tranne gli utenti esterni", "外部ユーザー以外のすべてのユーザー", "Сыртқы пайдаланушылардан басқасының барлығы", "외부 사용자를 제외한 모든 사람", "Visi, izņemot ārējos lietotājus", "Visi, išskyrus išorinius vartotojus", "Semua orang kecuali pengguna luaran", "Alle bortsett fra eksterne brukere", "Wszyscy oprócz użytkowników zewnętrznych", "Todos exceto os usuários externos", "Todos exceto os utilizadores externos", "Toată lumea, cu excepția utilizatorilor externi", "Все, кроме внешних пользователей", "Сви осим спољних корисника", "Svi osim spoljnih korisnika", "Všetci okrem externých používateľov", "Vsi razen zunanji uporabniki", "Todos excepto los usuarios externos", "Alla utom externa användare", "ทุกคนยกเว้นผู้ใช้ภายนอก", "Dış kullanıcılar hariç herkes", "Усі, крім зовнішніх користувачів", "Tất cả mọi người trừ người dùng bên ngoài");

   public static boolean isFullAccessSite(String permissionStr) {
      return FULL_ACCESS_SITE_STRINGS.contains(permissionStr);
   }
}
