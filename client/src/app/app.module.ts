import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

// Angular Material Imports
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';

// Components
import { AppComponent } from './app.component';
import { HomeComponent } from './components/home/home.component';
import { GroupFinderComponent } from './components/group-finder/group-finder.component';
import { SetlistExplorerComponent } from './components/setlist-explorer/setlist-explorer.component';
import { ProfileComponent } from './components/profile/profile.component';
import { ConcertDetailsComponent } from './components/concert-details/concert-details.component';
import { provideHttpClient } from '@angular/common/http';
import { ConcertService } from './services/concert.service';

// Routes configuration
const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'groups', component: GroupFinderComponent },
  { path: 'setlists', component: SetlistExplorerComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'concerts/:id', component: ConcertDetailsComponent },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
];

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    GroupFinderComponent,
    SetlistExplorerComponent,
    ProfileComponent,
    ConcertDetailsComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    RouterModule.forRoot(routes),

    // Material Modules
    MatToolbarModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule
  ],
  providers: [provideHttpClient(), ConcertService],
  bootstrap: [AppComponent]
})
export class AppModule { }